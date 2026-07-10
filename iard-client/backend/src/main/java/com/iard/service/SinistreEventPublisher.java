package com.iard.service;

import com.iard.config.KafkaConfig;
import com.iard.entity.Sinistre;
import com.iard.event.SinistreDeclareEvent;
import com.iard.repository.SinistreRepository;
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
 * Publication de l'événement SinistreDeclare sur Kafka.
 * La publication a lieu APRÈS le commit de la déclaration : un sinistre non persisté
 * n'est jamais publié. Si Kafka est indisponible, le sinistre reste en base
 * (eventPublie = false) et la publication est rejouée par le rattrapage planifié.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SinistreEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SinistreRepository sinistreRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onSinistreCree(SinistreDeclarationService.SinistreCreeEvent event) {
        sinistreRepository.findById(event.sinistreId()).ifPresent(this::publier);
    }

    /**
     * Rattrapage : republie les sinistres dont l'événement n'a pas pu être envoyé.
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 15000)
    @Transactional(readOnly = true)
    public void rattraperEventsNonPublies() {
        List<Sinistre> enAttente = sinistreRepository.findByEventPublieFalse();
        if (!enAttente.isEmpty()) {
            log.info("Rattrapage Kafka : {} événement(s) SinistreDeclare à republier", enAttente.size());
            enAttente.forEach(this::publier);
        }
    }

    private void publier(Sinistre sinistre) {
        SinistreDeclareEvent event = SinistreDeclareEvent.builder()
                .sinistreId(sinistre.getId())
                .numeroSinistre(sinistre.getNumeroSinistre())
                .contratId(sinistre.getContrat().getId())
                .numeroContrat(sinistre.getContrat().getNumeroContrat())
                .userId(sinistre.getUser().getId())
                .souscripteurNom(sinistre.getUser().getNom())
                .souscripteurPrenom(sinistre.getUser().getPrenom())
                .type(sinistre.getType().name())
                .dateSinistre(sinistre.getDateSinistre())
                .lieu(sinistre.getLieu())
                .description(sinistre.getDescription())
                .montantEstime(sinistre.getMontantEstime())
                .dateDeclaration(sinistre.getCreatedAt())
                .build();

        // Clé = sinistreId : garantit l'ordre des messages par sinistre
        Long sinistreId = sinistre.getId();
        String numero = sinistre.getNumeroSinistre();
        kafkaTemplate.send(KafkaConfig.TOPIC_SINISTRES_DECLARES, sinistreId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        sinistreRepository.marquerEventPublie(sinistreId);
                        log.info("Événement SinistreDeclare publié pour {} (offset {})",
                                numero, result.getRecordMetadata().offset());
                    } else {
                        log.warn("Échec de publication SinistreDeclare pour {} : {} (rattrapage planifié)",
                                numero, ex.getMessage());
                    }
                });
    }
}
