package com.iard.service;

import com.iard.entity.Contrat;
import com.iard.entity.StatutContrat;
import com.iard.repository.ContratRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Service dédié à l'activation des contrats.
 * Encapsule la transition EN_ATTENTE → ACTIF pour permettre
 * d'enrichir le processus ultérieurement (attestation, notification, etc.).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContratActivationService {

    private final ContratRepository contratRepository;

    /**
     * Active un contrat après un premier paiement réussi.
     * Met à jour le statut de EN_ATTENTE vers ACTIF et planifie la prochaine échéance.
     *
     * @param contrat Le contrat à activer
     * @return Le contrat mis à jour
     */
    @Transactional
    public Contrat activerContrat(Contrat contrat) {
        if (contrat.getStatut() != StatutContrat.EN_ATTENTE) {
            log.debug("Contrat {} déjà activé ou dans un autre statut: {}",
                    contrat.getNumeroContrat(), contrat.getStatut());
            return contrat;
        }

        log.info("Activation du contrat {} - Passage de EN_ATTENTE à ACTIF",
                contrat.getNumeroContrat());

        contrat.setStatut(StatutContrat.ACTIF);
        return contratRepository.save(contrat);
    }

    /**
     * Met à jour la prochaine date de prélèvement (+ 1 mois).
     *
     * @param contrat Le contrat à mettre à jour
     * @return Le contrat mis à jour
     */
    @Transactional
    public Contrat planifierProchaineEcheance(Contrat contrat) {
        LocalDate prochaineDate = contrat.getProchaineDatePrelevement().plusMonths(1);
        contrat.setProchaineDatePrelevement(prochaineDate);

        log.info("Prochaine échéance du contrat {} planifiée au {}",
                contrat.getNumeroContrat(), prochaineDate);

        return contratRepository.save(contrat);
    }

    /**
     * Active le contrat si nécessaire et planifie la prochaine échéance.
     * Appelé après un paiement réussi.
     *
     * @param contrat Le contrat à traiter
     * @return Le contrat mis à jour
     */
    @Transactional
    public Contrat traiterPaiementReussi(Contrat contrat) {
        // Activer si c'est le premier paiement (contrat en attente)
        if (contrat.getStatut() == StatutContrat.EN_ATTENTE) {
            activerContrat(contrat);
        }

        // Planifier la prochaine échéance
        return planifierProchaineEcheance(contrat);
    }
}
