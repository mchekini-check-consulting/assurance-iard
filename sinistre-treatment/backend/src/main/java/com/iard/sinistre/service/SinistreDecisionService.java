package com.iard.sinistre.service;

import com.iard.sinistre.dto.DecisionRequest;
import com.iard.sinistre.dto.DossierSinistreResponse;
import com.iard.sinistre.entity.DossierSinistre;
import com.iard.sinistre.entity.DossierStatutHistorique;
import com.iard.sinistre.entity.StatutDossier;
import com.iard.sinistre.repository.DossierSinistreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Règles métier d'instruction d'un dossier sinistre.
 * Service dédié pour pouvoir être enrichi plus tard.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SinistreDecisionService {

    // Transitions autorisées : A_TRAITER → EN_COURS_ANALYSE → APPROUVE ou REJETE
    private static final Map<StatutDossier, Set<StatutDossier>> TRANSITIONS_AUTORISEES = Map.of(
            StatutDossier.A_TRAITER, Set.of(StatutDossier.EN_COURS_ANALYSE),
            StatutDossier.EN_COURS_ANALYSE, Set.of(StatutDossier.APPROUVE, StatutDossier.REJETE),
            StatutDossier.APPROUVE, Set.of(),
            StatutDossier.REJETE, Set.of()
    );

    private final DossierSinistreRepository dossierRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public DossierSinistreResponse decider(Long dossierId, DecisionRequest request) {
        DossierSinistre dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable"));

        StatutDossier nouveauStatut = request.getStatut();
        if (!TRANSITIONS_AUTORISEES.get(dossier.getStatut()).contains(nouveauStatut)) {
            throw new IllegalArgumentException(
                    "Transition non autorisée : " + dossier.getStatut() + " → " + nouveauStatut);
        }

        if (nouveauStatut == StatutDossier.APPROUVE) {
            if (request.getMontantRembourse() == null) {
                throw new IllegalArgumentException("Le montant du remboursement est obligatoire en cas d'approbation");
            }
            if (request.getMontantRembourse().signum() < 0) {
                throw new IllegalArgumentException("Le montant du remboursement ne peut pas être négatif");
            }
            dossier.setMontantRembourse(request.getMontantRembourse());
        } else if (nouveauStatut == StatutDossier.REJETE) {
            if (request.getCommentaire() == null || request.getCommentaire().isBlank()) {
                throw new IllegalArgumentException("Le motif de la décision est obligatoire en cas de rejet");
            }
            // Montant à 0 et non modifiable en cas de rejet
            dossier.setMontantRembourse(BigDecimal.ZERO);
        }

        LocalDateTime dateDecision = LocalDateTime.now();
        dossier.setStatut(nouveauStatut);
        dossier.setCommentaireDecision(request.getCommentaire());
        dossier.setDecidePar(request.getDecidePar());
        dossier.setDateDecision(dateDecision);
        dossier.setSyncEnAttente(true);

        dossier.getHistoriqueStatuts().add(DossierStatutHistorique.builder()
                .dossier(dossier)
                .statut(nouveauStatut)
                .auteur(request.getDecidePar())
                .commentaire(request.getCommentaire())
                .dateChangement(dateDecision)
                .build());

        dossier = dossierRepository.save(dossier);
        log.info("Décision enregistrée sur le dossier {} : {} par {}",
                dossier.getNumeroSinistre(), nouveauStatut, request.getDecidePar());

        // Publication Kafka après commit pour ne jamais publier une décision non persistée
        applicationEventPublisher.publishEvent(new DecisionPriseEvent(dossier.getId()));

        return DossierSinistreResponse.fromEntity(dossier);
    }

    /**
     * Événement Spring interne déclenchant la publication Kafka après commit.
     */
    public record DecisionPriseEvent(Long dossierId) {
    }
}
