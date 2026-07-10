package com.iard.controller;

import com.iard.dto.DevisRequest;
import com.iard.dto.DevisResponse;
import com.iard.security.UserDetailsImpl;
import com.iard.service.DevisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devis")
@Tag(name = "Devis", description = "Parcours de devis habitation en 6 étapes : création, complétion des données "
        + "du risque, tarification, consultation et suppression. Authentification requise.")
@RequiredArgsConstructor
public class DevisController {

    private final DevisService devisService;

    @Operation(summary = "Créer un devis",
            description = "Initialise un devis habitation vide (statut BROUILLON, étape 1). "
                    + "Les données sont ensuite renseignées avec PUT /api/devis/{id}.")
    @PostMapping
    public ResponseEntity<DevisResponse> creerDevis(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        DevisResponse response = devisService.creerDevis(userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lister mes devis",
            description = "Retourne tous les devis de l'utilisateur connecté, du plus récent au plus ancien.")
    @GetMapping
    public ResponseEntity<List<DevisResponse>> listerDevis(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<DevisResponse> devis = devisService.listerDevis(userDetails.getUser().getId());
        return ResponseEntity.ok(devis);
    }

    @Operation(summary = "Consulter un devis",
            description = "Retourne le détail d'un devis : données du risque, étape courante et tarif s'il est calculé.")
    @GetMapping("/{id}")
    public ResponseEntity<DevisResponse> getDevis(
            @Parameter(description = "Identifiant du devis", example = "42")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DevisResponse response = devisService.getDevis(id, userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Compléter un devis (sauvegarde d'étape)",
            description = "Met à jour les données du risque ; seuls les champs non nuls sont appliqués, ce qui "
                    + "permet de sauvegarder étape par étape (bien, occupation, sécurité, contenu, antécédents, "
                    + "formule et options). Peut aussi être appelé une seule fois avec toutes les données.")
    @PutMapping("/{id}")
    public ResponseEntity<DevisResponse> sauvegarderEtape(
            @Parameter(description = "Identifiant du devis à compléter", example = "42")
            @PathVariable Long id,
            @RequestBody DevisRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DevisResponse response = devisService.sauvegarderEtape(id, userDetails.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Tarifer un devis",
            description = "Calcule la prime à partir des données du risque (la formule doit être renseignée). "
                    + "Le devis passe au statut DEVIS et peut alors être transformé en contrat.")
    @PostMapping("/{id}/tarifier")
    public ResponseEntity<DevisResponse> calculerTarif(
            @Parameter(description = "Identifiant du devis complété à tarifer", example = "42")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DevisResponse response = devisService.calculerTarif(id, userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Supprimer un devis",
            description = "Supprime définitivement un devis de l'utilisateur connecté.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerDevis(
            @Parameter(description = "Identifiant du devis à supprimer", example = "42")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        devisService.supprimerDevis(id, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Statistiques de mes devis",
            description = "Compteurs pour le tableau de bord : devis en brouillon et devis finalisés.")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(Map.of(
                "brouillons", devisService.compterDevisBrouillon(userId),
                "devis", devisService.compterDevisTermines(userId)
        ));
    }
}
