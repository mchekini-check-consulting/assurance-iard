package com.iard.controller;

import com.iard.dto.*;
import com.iard.security.UserDetailsImpl;
import com.iard.service.KycService;
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
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    /**
     * Récupère le statut KYC de l'utilisateur.
     */
    @GetMapping("/status")
    public ResponseEntity<KycStatusResponse> getStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        KycStatusResponse status = kycService.getKycStatus(userDetails.getUser().getId());
        return ResponseEntity.ok(status);
    }

    /**
     * Upload d'un titre de séjour.
     */
    @PostMapping(value = "/upload/titre-sejour", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TitreSejourExtraction> uploadTitreSejour(
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
    @PostMapping(value = "/upload/rib", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RibExtraction> uploadRib(
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
    @PostMapping("/verify")
    public ResponseEntity<KycVerificationResult> verify(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        KycVerificationResult result = kycService.verifyKyc(userDetails.getUser().getId());
        return ResponseEntity.ok(result);
    }

    /**
     * Liste les documents de l'utilisateur.
     */
    @GetMapping("/documents")
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<DocumentResponse> documents = kycService.getDocuments(userDetails.getUser().getId());
        return ResponseEntity.ok(documents);
    }

    /**
     * Récupère un document spécifique.
     */
    @GetMapping("/documents/{id}")
    public ResponseEntity<byte[]> getDocumentContent(
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
