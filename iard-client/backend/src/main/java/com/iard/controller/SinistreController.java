package com.iard.controller;

import com.iard.dto.DeclarationSinistreRequest;
import com.iard.dto.SinistreResponse;
import com.iard.entity.SinistrePieceJointe;
import com.iard.security.UserDetailsImpl;
import com.iard.service.SinistreDeclarationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/sinistres")
@RequiredArgsConstructor
public class SinistreController {

    private final SinistreDeclarationService sinistreDeclarationService;

    /**
     * Déclare un sinistre (multipart : données JSON + pièces jointes optionnelles).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SinistreResponse> declarerSinistre(
            @RequestPart("declaration") @Valid DeclarationSinistreRequest declaration,
            @RequestPart(value = "fichiers", required = false) List<MultipartFile> fichiers,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SinistreResponse response = sinistreDeclarationService.declarerSinistre(
                userDetails.getUser().getId(), declaration, fichiers);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Liste les sinistres du souscripteur connecté.
     */
    @GetMapping
    public ResponseEntity<List<SinistreResponse>> listerSinistres(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(sinistreDeclarationService.listerSinistres(userDetails.getUser().getId()));
    }

    /**
     * Détail d'un sinistre (timeline des statuts incluse).
     */
    @GetMapping("/{id}")
    public ResponseEntity<SinistreResponse> getSinistre(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(sinistreDeclarationService.getSinistre(id, userDetails.getUser().getId()));
    }

    /**
     * Téléchargement d'une pièce jointe.
     */
    @GetMapping("/{id}/pieces-jointes/{pieceId}")
    public ResponseEntity<byte[]> getPieceJointe(
            @PathVariable Long id,
            @PathVariable Long pieceId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        SinistrePieceJointe piece = sinistreDeclarationService.getPieceJointe(
                id, pieceId, userDetails.getUser().getId());
        byte[] contenu = Files.readAllBytes(Paths.get(piece.getPath()));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        piece.getContentType() != null ? piece.getContentType() : "application/octet-stream"))
                .header("Content-Disposition", "inline; filename=\"" + piece.getNomFichier() + "\"")
                .body(contenu);
    }
}
