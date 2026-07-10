package com.iard.service;

import com.iard.entity.Sinistre;
import com.iard.entity.SinistreStatutHistorique;
import com.iard.entity.StatutSinistre;
import com.iard.event.SinistreDecideEvent;
import com.iard.repository.SinistreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mise à jour du suivi d'un sinistre à partir des décisions reçues de sinistre-treatment.
 * Service dédié pour pouvoir être enrichi plus tard (notification, attestation de prise en charge).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SinistreSuiviService {

    private final SinistreRepository sinistreRepository;

    @Transactional
    public void appliquerDecision(SinistreDecideEvent event) {
        if (event.getSinistreId() == null || event.getStatut() == null) {
            throw new IllegalArgumentException("Événement SinistreDecide invalide : sinistreId et statut requis");
        }

        Sinistre sinistre = sinistreRepository.findById(event.getSinistreId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Sinistre " + event.getSinistreId() + " inconnu de la plateforme de souscription"));

        // Idempotence : un événement déjà appliqué (ou plus ancien que la dernière décision) est ignoré
        if (sinistre.getDateDecision() != null && event.getDateDecision() != null
                && !event.getDateDecision().isAfter(sinistre.getDateDecision())) {
            log.info("Événement SinistreDecide ignoré pour {} (décision du {} déjà appliquée)",
                    sinistre.getNumeroSinistre(), event.getDateDecision());
            return;
        }

        StatutSinistre nouveauStatut = convertirStatut(event.getStatut());

        if (sinistre.getStatut() != nouveauStatut) {
            sinistre.setStatut(nouveauStatut);
            sinistre.getHistoriqueStatuts().add(SinistreStatutHistorique.builder()
                    .sinistre(sinistre)
                    .statut(nouveauStatut)
                    .dateChangement(event.getDateDecision() != null
                            ? event.getDateDecision()
                            : java.time.LocalDateTime.now())
                    .build());
        }
        sinistre.setMontantRembourse(event.getMontantRembourse());
        sinistre.setCommentaireDecision(event.getCommentaireDecision());
        sinistre.setDateDecision(event.getDateDecision());

        sinistreRepository.save(sinistre);
        log.info("Sinistre {} synchronisé : statut {}, montant remboursé {}",
                sinistre.getNumeroSinistre(), nouveauStatut, event.getMontantRembourse());
    }

    private StatutSinistre convertirStatut(String statut) {
        // A_TRAITER côté sinistre-treatment correspond à DECLARE côté plateforme
        if ("A_TRAITER".equals(statut)) {
            return StatutSinistre.DECLARE;
        }
        try {
            return StatutSinistre.valueOf(statut);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut inconnu dans l'événement SinistreDecide : " + statut);
        }
    }
}
