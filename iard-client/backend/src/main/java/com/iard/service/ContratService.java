package com.iard.service;

import com.iard.dto.ContratResponse;
import com.iard.entity.*;
import com.iard.repository.ContratRepository;
import com.iard.repository.DevisRepository;
import com.iard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContratService {

    private final ContratRepository contratRepository;
    private final DevisRepository devisRepository;
    private final UserRepository userRepository;
    private final SignatureService signatureService;
    private final PdfService pdfService;
    private final KycService kycService;
    private final PaiementService paiementService;
    private final FactureService factureService;

    /**
     * Génère un contrat à partir d'un devis.
     * Le contrat est créé en attente de signature.
     */
    @Transactional
    public ContratResponse genererContrat(Long devisId, Long userId) {
        log.info("Génération du contrat pour le devis {} par l'utilisateur {}", devisId, userId);

        // Vérifier que le devis existe et appartient à l'utilisateur
        Devis devis = devisRepository.findByIdAndUserId(devisId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Devis non trouvé"));

        // Vérifier que le devis est en statut DEVIS ou PROPOSITION
        if (devis.getStatut() != StatutDevis.DEVIS && devis.getStatut() != StatutDevis.PROPOSITION) {
            throw new IllegalStateException("Le devis doit être finalisé pour générer un contrat");
        }

        // Vérifier que le KYC est validé
        if (!kycService.isKycVerified(userId)) {
            throw new IllegalStateException("KYC_REQUIRED");
        }

        // Passer le devis en PROPOSITION si ce n'est pas déjà fait
        if (devis.getStatut() == StatutDevis.DEVIS) {
            devis.setStatut(StatutDevis.PROPOSITION);
            devisRepository.save(devis);
        }

        // Vérifier qu'un contrat n'existe pas déjà pour ce devis
        if (contratRepository.existsByDevisId(devisId)) {
            // Retourner le contrat existant
            Contrat existant = contratRepository.findByDevisId(devisId)
                    .orElseThrow(() -> new IllegalStateException("Contrat introuvable"));
            return toContratResponse(existant);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // Générer le numéro de contrat unique
        String numeroContrat = genererNumeroContrat();

        // Créer le contrat
        Contrat contrat = Contrat.builder()
                .numeroContrat(numeroContrat)
                .devis(devis)
                .user(user)
                .assure(devis.getAssure())
                .produit(devis.getProduit())
                .formule(devis.getResultatTarif().getFormule())
                .garanties(devis.getResultatTarif())
                .primeHT(devis.getResultatTarif().getPrimeHT())
                .taxes(devis.getResultatTarif().getTaxes())
                .primeTTC(devis.getResultatTarif().getPrimeTTC())
                .periodicite(Periodicite.ANNUELLE)
                .statut(StatutContrat.EN_ATTENTE)
                .build();

        contrat = contratRepository.save(contrat);
        log.info("Contrat {} créé avec succès", numeroContrat);

        // Générer le PDF initial (non signé)
        try {
            String pdfPath = pdfService.genererContratPdf(contrat, user, devis);
            contrat.setPdfPath(pdfPath);
            contrat = contratRepository.save(contrat);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF: {}", e.getMessage());
        }

        return toContratResponse(contrat);
    }

    /**
     * Signe un contrat avec vérification du code OTP.
     */
    @Transactional
    public ContratResponse signerContrat(Long contratId, Long userId, String code) {
        log.info("Signature du contrat {} par l'utilisateur {}", contratId, userId);

        Contrat contrat = contratRepository.findByIdAndUserId(contratId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Contrat non trouvé"));

        // Vérifier que le contrat est en attente
        if (contrat.getStatut() != StatutContrat.EN_ATTENTE) {
            throw new IllegalStateException("Le contrat n'est pas en attente de signature");
        }

        // Vérifier le code OTP
        if (!signatureService.verifierCode(code)) {
            throw new IllegalArgumentException("Code de signature invalide");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // Signer le contrat
        contrat.setDateSignature(signatureService.horodater());
        contrat.setSignatureId(signatureService.genererSignatureId());

        // Initialiser les données de prélèvement
        contrat.setProchaineDatePrelevement(LocalDate.now());
        BigDecimal montantMensuel = contrat.getPrimeTTC()
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        contrat.setMontantMensuelTTC(montantMensuel);

        // Mettre à jour le statut du devis
        Devis devis = contrat.getDevis();
        devis.setStatut(StatutDevis.TRANSFORME);
        devisRepository.save(devis);

        // Simuler le premier prélèvement immédiatement (au lieu d'attendre le
        // batch quotidien de 6h) : paiement + facture si succès.
        Paiement paiement = paiementService.executerPrelevement(contrat, LocalDate.now());
        if (paiement.getStatut() == StatutPaiement.SUCCES) {
            factureService.genererFacture(paiement, contrat, user);
        } else {
            log.warn("Premier prélèvement mocké en échec pour le contrat {} (montant {} € > seuil) ; "
                            + "le contrat est activé malgré tout, le batch retentera le prélèvement",
                    contrat.getNumeroContrat(), contrat.getMontantMensuelTTC());
        }

        // Le contrat est actif dès la signature, quel que soit le résultat du
        // prélèvement mocké (qui échoue volontairement au-delà de 30 €/mois)
        contrat.setStatut(StatutContrat.ACTIF);
        contrat.setProchaineDatePrelevement(LocalDate.now().plusMonths(1));

        // Régénérer le PDF avec la signature
        try {
            String pdfPath = pdfService.regenererPdfSigne(contrat, user, devis);
            contrat.setPdfPath(pdfPath);
        } catch (Exception e) {
            log.error("Erreur lors de la régénération du PDF signé: {}", e.getMessage());
        }

        contrat = contratRepository.save(contrat);
        log.info("Contrat {} signé et activé avec succès", contrat.getNumeroContrat());

        return toContratResponse(contrat);
    }

    /**
     * Liste tous les contrats d'un utilisateur.
     */
    public List<ContratResponse> listerContrats(Long userId) {
        return contratRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toContratResponse)
                .collect(Collectors.toList());
    }

    /**
     * Liste les contrats d'un utilisateur par statut.
     */
    public List<ContratResponse> listerContratsParStatut(Long userId, StatutContrat statut) {
        return contratRepository.findByUserIdAndStatutOrderByCreatedAtDesc(userId, statut)
                .stream()
                .map(this::toContratResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un contrat par son ID.
     */
    public ContratResponse getContrat(Long contratId, Long userId) {
        Contrat contrat = contratRepository.findByIdAndUserId(contratId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Contrat non trouvé"));
        return toContratResponse(contrat);
    }

    /**
     * Récupère le contenu PDF d'un contrat.
     */
    public byte[] getPdfContent(Long contratId, Long userId) {
        Contrat contrat = contratRepository.findByIdAndUserId(contratId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Contrat non trouvé"));

        if (contrat.getPdfPath() == null) {
            throw new IllegalStateException("PDF non disponible pour ce contrat");
        }

        try {
            return pdfService.getPdfContent(contrat.getPdfPath());
        } catch (Exception e) {
            log.error("Erreur lors de la lecture du PDF: {}", e.getMessage());
            throw new IllegalStateException("Impossible de lire le PDF");
        }
    }

    /**
     * Génère un numéro de contrat unique.
     */
    private String genererNumeroContrat() {
        String prefix = "CTR";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return prefix + "-" + timestamp + "-" + random;
    }

    /**
     * Convertit un Contrat en ContratResponse.
     */
    private ContratResponse toContratResponse(Contrat contrat) {
        User user = contrat.getUser();
        Devis devis = contrat.getDevis();

        return ContratResponse.builder()
                .id(contrat.getId())
                .numeroContrat(contrat.getNumeroContrat())
                .devisId(devis.getId())
                .produit(contrat.getProduit())
                .formule(contrat.getFormule())
                .garanties(contrat.getGaranties())
                .primeHT(contrat.getPrimeHT())
                .taxes(contrat.getTaxes())
                .primeTTC(contrat.getPrimeTTC())
                .periodicite(contrat.getPeriodicite())
                .statut(contrat.getStatut())
                .dateSignature(contrat.getDateSignature())
                .signatureId(contrat.getSignatureId())
                .pdfUrl("/api/contrats/" + contrat.getId() + "/pdf")
                .createdAt(contrat.getCreatedAt())
                .prochaineDatePrelevement(contrat.getProchaineDatePrelevement())
                .montantMensuelTTC(contrat.getMontantMensuelTTC())
                .donneesRisque(devis.getDonneesRisque())
                .assure(contrat.getAssure())
                .souscripteurNom(user.getNom())
                .souscripteurPrenom(user.getPrenom())
                .souscripteurEmail(user.getEmail())
                .build();
    }
}
