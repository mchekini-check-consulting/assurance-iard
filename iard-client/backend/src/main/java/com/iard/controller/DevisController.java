package com.iard.controller;

import com.iard.dto.DevisRequest;
import com.iard.dto.DevisResponse;
import com.iard.security.UserDetailsImpl;
import com.iard.service.DevisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devis")
@RequiredArgsConstructor
public class DevisController {

    private final DevisService devisService;

    @PostMapping
    public ResponseEntity<DevisResponse> creerDevis(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        DevisResponse response = devisService.creerDevis(userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DevisResponse>> listerDevis(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<DevisResponse> devis = devisService.listerDevis(userDetails.getUser().getId());
        return ResponseEntity.ok(devis);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DevisResponse> getDevis(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DevisResponse response = devisService.getDevis(id, userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DevisResponse> sauvegarderEtape(
            @PathVariable Long id,
            @RequestBody DevisRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DevisResponse response = devisService.sauvegarderEtape(id, userDetails.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/tarifier")
    public ResponseEntity<DevisResponse> calculerTarif(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DevisResponse response = devisService.calculerTarif(id, userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerDevis(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        devisService.supprimerDevis(id, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(Map.of(
                "brouillons", devisService.compterDevisBrouillon(userId),
                "devis", devisService.compterDevisTermines(userId)
        ));
    }
}
