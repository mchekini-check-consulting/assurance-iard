package com.iard.service;

import com.iard.entity.*;
import com.iard.repository.ContratRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service batch pour l'exécution des prélèvements quotidiens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrelevementBatchService {

    private final ContratRepository contratRepository;
    private final PaiementService paiementService;
    private final FactureService factureService;
    private final ContratActivationService contratActivationService;

    // Statuts éligibles au prélèvement
    private static final List<StatutContrat> STATUTS_ELIGIBLES = List.of(
            StatutContrat.EN_ATTENTE,
            StatutContrat.ACTIF
    );

    /**
     * Job planifié exécuté tous les jours à 6h00.
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void executerPrelevementsQuotidiens() {
        log.info("=== Démarrage du batch de prélèvements quotidiens ===");
        executerPrelevements(LocalDate.now());
    }

    /**
     * Exécute les prélèvements pour une date donnée.
     * Peut être appelé manuellement pour les tests.
     * Chaque contrat est traité dans une transaction séparée pour garantir l'isolation.
     *
     * @param datePrelevement La date de prélèvement (permet de tester avec une date spécifique)
     * @return Résultat du batch (nombre de succès/échecs)
     */
    public BatchResult executerPrelevements(LocalDate datePrelevement) {
        log.info("Exécution des prélèvements pour la date: {}", datePrelevement);

        List<Contrat> contratsEligibles = contratRepository.findContratsEligiblesPrelevement(
                datePrelevement, STATUTS_ELIGIBLES);

        log.info("{} contrat(s) éligible(s) au prélèvement", contratsEligibles.size());

        int succes = 0;
        int echecs = 0;

        for (Contrat contrat : contratsEligibles) {
            try {
                traiterContratIsolee(contrat.getId(), datePrelevement);
                succes++;
            } catch (Exception e) {
                log.error("Erreur lors du traitement du contrat {}: {}",
                        contrat.getNumeroContrat(), e.getMessage(), e);
                echecs++;
            }
        }

        log.info("=== Fin du batch: {} succès, {} échecs ===", succes, echecs);

        return new BatchResult(contratsEligibles.size(), succes, echecs);
    }

    /**
     * Traite un contrat dans une transaction isolée.
     * Récupère le contrat frais pour éviter les problèmes de détachement.
     */
    @Transactional
    public void traiterContratIsolee(Long contratId, LocalDate datePrelevement) {
        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new IllegalStateException("Contrat non trouvé: " + contratId));
        traiterContrat(contrat, datePrelevement);
    }

    /**
     * Traite un contrat individuel.
     * Isolé pour que l'échec d'un contrat n'affecte pas les autres.
     */
    private void traiterContrat(Contrat contrat, LocalDate datePrelevement) {
        log.info("Traitement du contrat {} - Statut: {} - Montant: {} €",
                contrat.getNumeroContrat(),
                contrat.getStatut(),
                contrat.getMontantMensuelTTC());

        // Exécuter le prélèvement
        Paiement paiement = paiementService.executerPrelevement(contrat, datePrelevement);

        if (paiement.getStatut() == StatutPaiement.SUCCES) {
            // Générer la facture
            factureService.genererFacture(paiement, contrat, contrat.getUser());

            // Activer le contrat si nécessaire et planifier la prochaine échéance
            contratActivationService.traiterPaiementReussi(contrat);

            log.info("Contrat {} traité avec succès - Prochaine échéance: {}",
                    contrat.getNumeroContrat(),
                    contrat.getProchaineDatePrelevement());
        } else {
            log.warn("Prélèvement échoué pour le contrat {} - Aucune facture générée",
                    contrat.getNumeroContrat());
        }
    }

    /**
     * Résultat d'exécution du batch.
     */
    public record BatchResult(int total, int succes, int echecs) {}
}
