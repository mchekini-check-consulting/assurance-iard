package com.iard.sinistre.controller;

import com.iard.sinistre.dto.DecisionRequest;
import com.iard.sinistre.dto.DossierSinistreResponse;
import com.iard.sinistre.service.DossierSinistreService;
import com.iard.sinistre.service.SinistreDecisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dossiers")
@RequiredArgsConstructor
public class DossierSinistreController {

    private final DossierSinistreService dossierSinistreService;
    private final SinistreDecisionService sinistreDecisionService;

    /**
     * Liste des dossiers de sinistres reçus.
     */
    @GetMapping
    public ResponseEntity<List<DossierSinistreResponse>> listerDossiers() {
        return ResponseEntity.ok(dossierSinistreService.listerDossiers());
    }

    /**
     * Détail d'un dossier (historique des statuts inclus).
     */
    @GetMapping("/{id}")
    public ResponseEntity<DossierSinistreResponse> getDossier(@PathVariable Long id) {
        return ResponseEntity.ok(dossierSinistreService.getDossier(id));
    }

    /**
     * Enregistre une décision du gestionnaire sur un dossier.
     */
    @PostMapping("/{id}/decision")
    public ResponseEntity<DossierSinistreResponse> decider(
            @PathVariable Long id,
            @Valid @RequestBody DecisionRequest request) {
        return ResponseEntity.ok(sinistreDecisionService.decider(id, request));
    }
}
