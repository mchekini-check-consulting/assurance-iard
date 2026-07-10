package com.iard.controller;

import com.iard.dto.*;
import com.iard.security.UserDetailsImpl;
import com.iard.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/kyc")
@Tag(name = "KYC", description = "Vérification d'identité (Know Your Customer) : upload des justificatifs "
        + "(titre de séjour, RIB), extraction OCR par GPT Vision et vérification. Un KYC vérifié est requis "
        + "pour générer un contrat. Authentification requise.")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    /**
     * Récupère le statut KYC de l'utilisateur.
     */
    @Operation(summary = "Statut KYC",
            description = "Retourne le statut de vérification de l'utilisateur (NON_DEMARRE, EN_COURS, VERIFIE, "
                    + "REJETE) et la liste des documents attendus ou fournis.")
    @GetMapping("/status")
    public ResponseEntity<KycStatusResponse> getStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        KycStatusResponse status = kycService.getKycStatus(userDetails.getUser().getId());
        return ResponseEntity.ok(status);
    }

    /**
     * Upload d'un titre de séjour.
     */
    @Operation(summary = "Uploader un titre de séjour",
            description = "Téléverse le titre de séjour (image ou PDF, 10 Mo max). Les champs (identité, numéro, "
                    + "validité) sont extraits automatiquement par OCR GPT Vision et retournés pour confirmation. "
                    + "Sans clé OpenAI configurée côté serveur, l'extraction est désactivée.")
    @PostMapping(value = "/upload/titre-sejour", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TitreSejourExtraction> uploadTitreSejour(
            @Parameter(description = "Fichier du titre de séjour (image ou PDF, 10 Mo max)")
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {

        validateFile(file);

        TitreSejourExtraction extraction = kycService.uploadTitreSejour(
                userDetails.getUser().getId(),
                file
        );
        return ResponseEntity.ok(extraction);
    }

    /**
     * Upload d'un RIB.
     */
    @Operation(summary = "Uploader un RIB",
            description = "Téléverse le relevé d'identité bancaire (image ou PDF, 10 Mo max). Le titulaire, "
                    + "l'IBAN et le BIC sont extraits automatiquement par OCR et l'IBAN est validé.")
    @PostMapping(value = "/upload/rib", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RibExtraction> uploadRib(
            @Parameter(description = "Fichier du RIB (image ou PDF, 10 Mo max)")
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {

        validateFile(file);

        RibExtraction extraction = kycService.uploadRib(
                userDetails.getUser().getId(),
                file
        );
        return ResponseEntity.ok(extraction);
    }

    /**
     * Lance la vérification KYC.
     */
    @Operation(summary = "Lancer la vérification KYC",
            description = "Vérifie la cohérence des documents uploadés (correspondance des identités, validité "
                    + "du titre, IBAN correct) et passe le statut à VERIFIE ou REJETE avec le détail des contrôles.")
    @PostMapping("/verify")
    public ResponseEntity<KycVerificationResult> verify(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        KycVerificationResult result = kycService.verifyKyc(userDetails.getUser().getId());
        return ResponseEntity.ok(result);
    }

    /**
     * Liste les documents de l'utilisateur.
     */
    @Operation(summary = "Lister mes documents KYC",
            description = "Retourne les justificatifs uploadés par l'utilisateur avec leur type et statut.")
    @GetMapping("/documents")
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<DocumentResponse> documents = kycService.getDocuments(userDetails.getUser().getId());
        return ResponseEntity.ok(documents);
    }

    /**
     * Récupère un document spécifique.
     */
    @Operation(summary = "Télécharger un document KYC")
    @GetMapping("/documents/{id}")
    public ResponseEntity<byte[]> getDocumentContent(
            @Parameter(description = "Identifiant du document", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        DocumentResponse doc = kycService.getDocument(id, userDetails.getUser().getId());
        byte[] content = kycService.getDocumentContent(id, userDetails.getUser().getId());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header("Content-Disposition", "inline; filename=\"" + doc.getNomFichier() + "\"")
                .body(content);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
            (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("Type de fichier non supporté. Utilisez une image ou un PDF.");
        }

        // Limite de taille : 10 MB
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Le fichier est trop volumineux (max 10 MB)");
        }
    }
}
