package com.iard.controller;

import com.iard.dto.FactureResponse;
import com.iard.security.UserDetailsImpl;
import com.iard.service.FactureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * Controller pour la gestion des factures.
 */
@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;

    /**
     * Liste toutes les factures de l'utilisateur connecté.
     */
    @GetMapping
    public ResponseEntity<List<FactureResponse>> getFactures(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<FactureResponse> factures = factureService.getFacturesUtilisateur(userDetails.getUser().getId());
        return ResponseEntity.ok(factures);
    }

    /**
     * Récupère les détails d'une facture.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FactureResponse> getFacture(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        FactureResponse facture = factureService.getFacture(id, userDetails.getUser().getId());
        return ResponseEntity.ok(facture);
    }

    /**
     * Télécharge le PDF d'une facture.
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {

        Long userId = userDetails.getUser().getId();
        FactureResponse facture = factureService.getFacture(id, userId);
        byte[] pdfContent = factureService.getPdfContent(id, userId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + facture.getNumeroFacture() + ".pdf\"")
                .body(pdfContent);
    }
}
