package com.iard.controller;

import com.iard.dto.DeclarationSinistreRequest;
import com.iard.dto.SinistreResponse;
import com.iard.entity.SinistrePieceJointe;
import com.iard.security.UserDetailsImpl;
import com.iard.service.SinistreDeclarationService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Sinistres", description = "Déclaration et suivi des sinistres. La déclaration est publiée sur Kafka "
        + "et traitée par l'application sinistre-treatment, qui renvoie sa décision de façon asynchrone. "
        + "Authentification requise.")
@RequiredArgsConstructor
public class SinistreController {

    private final SinistreDeclarationService sinistreDeclarationService;

    /**
     * Déclare un sinistre (multipart : données JSON + pièces jointes optionnelles).
     */
    @Operation(summary = "Déclarer un sinistre",
            description = "Déclare un sinistre sur un contrat actif. Requête multipart : partie `declaration` "
                    + "(JSON : contratId, type de sinistre, date, description…) et partie `fichiers` optionnelle "
                    + "(photos, factures — 5 Mo max par fichier). Le dossier est transmis à l'équipe de gestion "
                    + "des sinistres via Kafka.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SinistreResponse> declarerSinistre(
            @Parameter(description = "Données de la déclaration (JSON)")
            @RequestPart("declaration") @Valid DeclarationSinistreRequest declaration,
            @Parameter(description = "Pièces jointes optionnelles (photos, factures…)")
            @RequestPart(value = "fichiers", required = false) List<MultipartFile> fichiers,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SinistreResponse response = sinistreDeclarationService.declarerSinistre(
                userDetails.getUser().getId(), declaration, fichiers);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Liste les sinistres du souscripteur connecté.
     */
    @Operation(summary = "Lister mes sinistres",
            description = "Retourne les sinistres déclarés par l'utilisateur connecté avec leur statut de traitement.")
    @GetMapping
    public ResponseEntity<List<SinistreResponse>> listerSinistres(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(sinistreDeclarationService.listerSinistres(userDetails.getUser().getId()));
    }

    /**
     * Détail d'un sinistre (timeline des statuts incluse).
     */
    @Operation(summary = "Consulter un sinistre",
            description = "Retourne le détail d'un sinistre : statut, historique et décision si elle est rendue.")
    @GetMapping("/{id}")
    public ResponseEntity<SinistreResponse> getSinistre(
            @Parameter(description = "Identifiant du sinistre", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(sinistreDeclarationService.getSinistre(id, userDetails.getUser().getId()));
    }

    /**
     * Téléchargement d'une pièce jointe.
     */
    @Operation(summary = "Télécharger une pièce jointe d'un sinistre")
    @GetMapping("/{id}/pieces-jointes/{pieceId}")
    public ResponseEntity<byte[]> getPieceJointe(
            @Parameter(description = "Identifiant du sinistre", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Identifiant de la pièce jointe", example = "1")
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
