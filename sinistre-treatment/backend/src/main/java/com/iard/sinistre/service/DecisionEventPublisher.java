package com.iard.sinistre.service;

import com.iard.sinistre.config.KafkaConfig;
import com.iard.sinistre.entity.DossierSinistre;
import com.iard.sinistre.event.SinistreDecideEvent;
import com.iard.sinistre.repository.DossierSinistreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * Publication de l'événement SinistreDecide sur Kafka, après commit de la décision.
 * Si Kafka est indisponible, la décision reste en base (syncEnAttente = true)
 * et la publication est rejouée par le rattrapage planifié. L'événement reflétant
 * l'état courant du dossier, une republication est sans risque (consumer idempotent).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DossierSinistreRepository dossierRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onDecisionPrise(SinistreDecisionService.DecisionPriseEvent event) {
        dossierRepository.findById(event.dossierId()).ifPresent(this::publier);
    }

    /**
     * Rattrapage : republie les décisions dont l'événement n'a pas pu être envoyé.
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 15000)
    @Transactional(readOnly = true)
    public void rattraperDecisionsNonSynchronisees() {
        List<DossierSinistre> enAttente = dossierRepository.findBySyncEnAttenteTrue();
        if (!enAttente.isEmpty()) {
            log.info("Rattrapage Kafka : {} événement(s) SinistreDecide à republier", enAttente.size());
            enAttente.forEach(this::publier);
        }
    }

    private void publier(DossierSinistre dossier) {
        SinistreDecideEvent event = SinistreDecideEvent.builder()
                .sinistreId(dossier.getSinistreId())
                .numeroSinistre(dossier.getNumeroSinistre())
                .statut(dossier.getStatut().name())
                .montantRembourse(dossier.getMontantRembourse())
                .commentaireDecision(dossier.getCommentaireDecision())
                .decidePar(dossier.getDecidePar())
                .dateDecision(dossier.getDateDecision())
                .build();

        // Clé = sinistreId : garantit l'ordre des messages par sinistre
        Long dossierId = dossier.getId();
        String numero = dossier.getNumeroSinistre();
        kafkaTemplate.send(KafkaConfig.TOPIC_SINISTRES_DECISIONS, dossier.getSinistreId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        dossierRepository.marquerSynchronise(dossierId);
                        log.info("Événement SinistreDecide publié pour {} (offset {})",
                                numero, result.getRecordMetadata().offset());
                    } else {
                        log.warn("Échec de publication SinistreDecide pour {} : {} (rattrapage planifié)",
                                numero, ex.getMessage());
                    }
                });
    }
}
