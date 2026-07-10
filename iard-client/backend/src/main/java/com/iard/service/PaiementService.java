package com.iard.service;

import com.iard.entity.Contrat;
import com.iard.entity.Paiement;
import com.iard.entity.StatutPaiement;
import com.iard.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service de gestion des paiements.
 * Le prélèvement est mocké : montant > 30€ = échec, montant <= 30€ = succès.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaiementService {

    private final PaiementRepository paiementRepository;

    private static final BigDecimal SEUIL_SUCCES = new BigDecimal("30.00");
    private static final DateTimeFormatter PERIODE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Exécute un prélèvement mocké pour un contrat.
     *
     * @param contrat Le contrat à prélever
     * @param datePrelevement La date du prélèvement
     * @return Le paiement créé avec son statut
     */
    @Transactional
    public Paiement executerPrelevement(Contrat contrat, LocalDate datePrelevement) {
        String periode = datePrelevement.format(PERIODE_FORMATTER);

        // Vérifier l'idempotence : ne pas prélever deux fois pour la même période
        if (paiementRepository.existsByContratIdAndPeriode(contrat.getId(), periode)) {
            log.info("Prélèvement déjà effectué pour le contrat {} période {}",
                    contrat.getNumeroContrat(), periode);
            return paiementRepository.findByContratIdAndPeriode(contrat.getId(), periode)
                    .orElseThrow();
        }

        // Récupérer le montant mensuel, ou le calculer si non défini (contrats existants)
        BigDecimal montant = contrat.getMontantMensuelTTC();
        if (montant == null) {
            // Pour les contrats créés avant l'ajout de ce champ, calculer depuis primeTTC
            BigDecimal primeTTC = contrat.getPrimeTTC();
            if (primeTTC == null) {
                throw new IllegalStateException("Le contrat " + contrat.getNumeroContrat()
                        + " n'a pas de prime TTC définie");
            }
            montant = primeTTC.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            // Mettre à jour le contrat pour les prochains prélèvements
            contrat.setMontantMensuelTTC(montant);
            log.info("Montant mensuel calculé pour contrat {} : {} €",
                    contrat.getNumeroContrat(), montant);
        }

        // Règle de mock : montant > 30€ = échec
        StatutPaiement statut = montant.compareTo(SEUIL_SUCCES) > 0
                ? StatutPaiement.FAILED
                : StatutPaiement.SUCCES;

        log.info("Prélèvement {} pour contrat {} - Montant: {} € - Statut: {}",
                periode, contrat.getNumeroContrat(), montant, statut);

        Paiement paiement = Paiement.builder()
                .contrat(contrat)
                .montant(montant)
                .datePrelevement(datePrelevement)
                .statut(statut)
                .periode(periode)
                .build();

        return paiementRepository.save(paiement);
    }

    /**
     * Vérifie si un prélèvement a déjà été effectué pour un contrat et une période.
     */
    public boolean prelevementDejaEffectue(Long contratId, String periode) {
        return paiementRepository.existsByContratIdAndPeriode(contratId, periode);
    }
}
