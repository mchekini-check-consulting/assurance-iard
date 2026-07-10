package com.iard.service;

import com.iard.dto.*;
import com.iard.entity.*;
import com.iard.repository.DocumentRepository;
import com.iard.repository.KycVerificationRepository;
import com.iard.repository.UserRepository;
import com.iard.util.IbanValidator;
import com.iard.util.NameMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycService {

    private final KycVerificationRepository kycRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentExtractionService extractionService;

    /**
     * Récupère le statut KYC d'un utilisateur.
     */
    public KycStatusResponse getKycStatus(Long userId) {
        Optional<KycVerification> kycOpt = kycRepository.findByUserId(userId);

        boolean titreSejour = documentRepository.existsByUserIdAndType(userId, TypeDocument.TITRE_SEJOUR);
        boolean rib = documentRepository.existsByUserIdAndType(userId, TypeDocument.RIB);

        if (kycOpt.isEmpty()) {
            return KycStatusResponse.builder()
                    .statut(StatutKyc.NON_VERIFIE)
                    .titreSejour_uploaded(titreSejour)
                    .rib_uploaded(rib)
                    .build();
        }

        KycVerification kyc = kycOpt.get();
        return KycStatusResponse.builder()
                .id(kyc.getId())
                .statut(kyc.getStatut())
                .donneesExtraites(kyc.getDonneesExtraites())
                .dateVerification(kyc.getDateVerification())
                .motifRefus(kyc.getMotifRefus())
                .titreSejour_uploaded(titreSejour)
                .rib_uploaded(rib)
                .build();
    }

    /**
     * Téléverse et analyse un titre de séjour.
     */
    @Transactional
    public TitreSejourExtraction uploadTitreSejour(Long userId, MultipartFile file) throws IOException {
        log.info("Upload titre de séjour pour utilisateur {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // Sauvegarder le document
        KycDocument doc = documentRepository.findByUserIdAndType(userId, TypeDocument.TITRE_SEJOUR)
                .orElse(KycDocument.builder()
                        .user(user)
                        .type(TypeDocument.TITRE_SEJOUR)
                        .build());

        doc.setNomFichier(file.getOriginalFilename());
        doc.setContentType(file.getContentType());
        doc.setContenu(file.getBytes());
        doc.setCreatedAt(LocalDateTime.now());
        documentRepository.save(doc);

        // Extraire les données via OCR
        TitreSejourExtraction extraction = extractionService.extractTitreSejour(
                file.getBytes(),
                file.getContentType()
        );

        // Mettre à jour le KYC avec les données extraites
        if (extraction.isExtractionReussie()) {
            updateKycWithTitreSejour(userId, extraction);
        }

        return extraction;
    }

    /**
     * Téléverse et analyse un RIB.
     */
    @Transactional
    public RibExtraction uploadRib(Long userId, MultipartFile file) throws IOException {
        log.info("Upload RIB pour utilisateur {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // Sauvegarder le document
        KycDocument doc = documentRepository.findByUserIdAndType(userId, TypeDocument.RIB)
                .orElse(KycDocument.builder()
                        .user(user)
                        .type(TypeDocument.RIB)
                        .build());

        doc.setNomFichier(file.getOriginalFilename());
        doc.setContentType(file.getContentType());
        doc.setContenu(file.getBytes());
        doc.setCreatedAt(LocalDateTime.now());
        documentRepository.save(doc);

        // Extraire les données via OCR
        RibExtraction extraction = extractionService.extractRib(
                file.getBytes(),
                file.getContentType()
        );

        // Mettre à jour le KYC avec les données extraites
        if (extraction.isExtractionReussie()) {
            updateKycWithRib(userId, extraction);
        }

        return extraction;
    }

    /**
     * Lance la vérification KYC.
     */
    @Transactional
    public KycVerificationResult verifyKyc(Long userId) {
        log.info("Vérification KYC pour utilisateur {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        KycVerification kyc = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Veuillez d'abord téléverser vos documents"));

        DonneesExtraitesKyc donnees = kyc.getDonneesExtraites();
        if (donnees == null) {
            return KycVerificationResult.builder()
                    .statut(StatutKyc.REFUSE)
                    .success(false)
                    .erreurs(List.of("Aucune donnée extraite des documents"))
                    .message("Veuillez téléverser vos documents")
                    .build();
        }

        List<String> erreurs = new ArrayList<>();

        // Vérifier le titre de séjour
        if (donnees.getTitreSejour_nom() == null || donnees.getTitreSejour_prenom() == null) {
            erreurs.add("Titre de séjour non téléversé ou illisible");
        } else {
            // Vérifier le nom/prénom
            boolean nomMatch = NameMatcher.matches(
                    donnees.getTitreSejour_nom(),
                    donnees.getTitreSejour_prenom(),
                    user.getNom(),
                    user.getPrenom()
            );
            if (!nomMatch) {
                erreurs.add("Le nom/prénom du titre de séjour ne correspond pas à votre profil");
            }

            // Vérifier la date d'expiration
            if (donnees.getTitreSejour_dateExpiration() != null) {
                if (donnees.getTitreSejour_dateExpiration().isBefore(LocalDate.now())) {
                    erreurs.add("Le titre de séjour est expiré");
                }
            } else {
                erreurs.add("Date d'expiration du titre de séjour non lisible");
            }
        }

        // Vérifier le RIB
        if (donnees.getRib_nom() == null || donnees.getRib_iban() == null) {
            erreurs.add("RIB non téléversé ou illisible");
        } else {
            // Vérifier le nom/prénom
            boolean nomMatch = NameMatcher.matches(
                    donnees.getRib_nom(),
                    donnees.getRib_prenom() != null ? donnees.getRib_prenom() : "",
                    user.getNom(),
                    user.getPrenom()
            );
            if (!nomMatch) {
                // Essayer avec le nom complet dans un seul champ
                String ribFullName = donnees.getRib_nom() +
                        (donnees.getRib_prenom() != null ? " " + donnees.getRib_prenom() : "");
                nomMatch = NameMatcher.matchesSingleField(ribFullName, user.getNom(), user.getPrenom());
            }
            if (!nomMatch) {
                erreurs.add("Le nom du RIB ne correspond pas à votre profil");
            }

            // Vérifier l'IBAN
            if (!IbanValidator.isValidFrenchIban(donnees.getRib_iban())) {
                erreurs.add("L'IBAN n'est pas valide (format français ou checksum invalide)");
            }
        }

        // Résultat
        if (erreurs.isEmpty()) {
            kyc.setStatut(StatutKyc.VERIFIE);
            kyc.setDateVerification(LocalDateTime.now());
            kyc.setMotifRefus(null);
            kycRepository.save(kyc);

            return KycVerificationResult.builder()
                    .statut(StatutKyc.VERIFIE)
                    .donneesExtraites(donnees)
                    .success(true)
                    .erreurs(List.of())
                    .message("KYC validé avec succès")
                    .build();
        } else {
            kyc.setStatut(StatutKyc.REFUSE);
            kyc.setDateVerification(LocalDateTime.now());
            kyc.setMotifRefus(String.join("; ", erreurs));
            kycRepository.save(kyc);

            return KycVerificationResult.builder()
                    .statut(StatutKyc.REFUSE)
                    .donneesExtraites(donnees)
                    .success(false)
                    .erreurs(erreurs)
                    .message("KYC refusé : " + erreurs.get(0))
                    .build();
        }
    }

    /**
     * Liste les documents d'un utilisateur.
     */
    public List<DocumentResponse> getDocuments(Long userId) {
        return documentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDocumentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère le contenu d'un document.
     */
    public byte[] getDocumentContent(Long documentId, Long userId) {
        KycDocument doc = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Document non trouvé"));
        return doc.getContenu();
    }

    /**
     * Récupère les métadonnées d'un document.
     */
    public DocumentResponse getDocument(Long documentId, Long userId) {
        KycDocument doc = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Document non trouvé"));
        return toDocumentResponse(doc);
    }

    /**
     * Vérifie si le KYC est validé pour un utilisateur.
     */
    public boolean isKycVerified(Long userId) {
        return kycRepository.existsByUserIdAndStatut(userId, StatutKyc.VERIFIE);
    }

    private void updateKycWithTitreSejour(Long userId, TitreSejourExtraction extraction) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        KycVerification kyc = kycRepository.findByUserId(userId)
                .orElse(KycVerification.builder()
                        .user(user)
                        .statut(StatutKyc.EN_COURS)
                        .build());

        DonneesExtraitesKyc donnees = kyc.getDonneesExtraites();
        if (donnees == null) {
            donnees = new DonneesExtraitesKyc();
        }

        donnees.setTitreSejour_nom(extraction.getNom());
        donnees.setTitreSejour_prenom(extraction.getPrenom());
        donnees.setTitreSejour_numero(extraction.getNumero());

        if (extraction.getDateExpiration() != null) {
            try {
                donnees.setTitreSejour_dateExpiration(
                        LocalDate.parse(extraction.getDateExpiration(), DateTimeFormatter.ISO_LOCAL_DATE)
                );
            } catch (DateTimeParseException e) {
                log.warn("Format de date invalide: {}", extraction.getDateExpiration());
            }
        }

        kyc.setDonneesExtraites(donnees);
        kyc.setStatut(StatutKyc.EN_COURS);
        kycRepository.save(kyc);
    }

    private void updateKycWithRib(Long userId, RibExtraction extraction) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        KycVerification kyc = kycRepository.findByUserId(userId)
                .orElse(KycVerification.builder()
                        .user(user)
                        .statut(StatutKyc.EN_COURS)
                        .build());

        DonneesExtraitesKyc donnees = kyc.getDonneesExtraites();
        if (donnees == null) {
            donnees = new DonneesExtraitesKyc();
        }

        donnees.setRib_nom(extraction.getNom());
        donnees.setRib_prenom(extraction.getPrenom());
        donnees.setRib_banque(extraction.getBanque());
        donnees.setRib_iban(extraction.getIban());

        kyc.setDonneesExtraites(donnees);
        kyc.setStatut(StatutKyc.EN_COURS);
        kycRepository.save(kyc);
    }

    private DocumentResponse toDocumentResponse(KycDocument doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .type(doc.getType())
                .nomFichier(doc.getNomFichier())
                .contentType(doc.getContentType())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
