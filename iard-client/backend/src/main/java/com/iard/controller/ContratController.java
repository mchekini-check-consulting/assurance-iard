package com.iard.controller;

import com.iard.dto.AvenantRequest;
import com.iard.dto.ContratResponse;
import com.iard.dto.DevisResponse;
import com.iard.dto.SignatureRequest;
import com.iard.entity.StatutContrat;
import com.iard.security.UserDetailsImpl;
import com.iard.service.ContratService;
import com.iard.service.DevisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contrats")
@RequiredArgsConstructor
public class ContratController {

    private final ContratService contratService;
    private final DevisService devisService;

    /**
     * Génère un contrat à partir d'un devis.
     */
    @PostMapping("/generer/{devisId}")
    public ResponseEntity<ContratResponse> genererContrat(
            @PathVariable Long devisId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ContratResponse response = contratService.genererContrat(devisId, userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Liste tous les contrats de l'utilisateur.
     */
    @GetMapping
    public ResponseEntity<List<ContratResponse>> listerContrats(
            @RequestParam(required = false) StatutContrat statut,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        List<ContratResponse> contrats;

        if (statut != null) {
            contrats = contratService.listerContratsParStatut(userId, statut);
        } else {
            contrats = contratService.listerContrats(userId);
        }

        return ResponseEntity.ok(contrats);
    }

    /**
     * Récupère un contrat par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContratResponse> getContrat(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ContratResponse response = contratService.getContrat(id, userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Modifie les garanties d'un contrat actif (selfcare) : crée un devis
     * d'avenant tarifé, à souscrire et signer via le parcours habituel.
     * À la signature du nouveau contrat, le contrat d'origine est résilié.
     */
    @PostMapping("/{id}/avenant")
    public ResponseEntity<DevisResponse> creerAvenant(
            @PathVariable Long id,
            @RequestBody AvenantRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DevisResponse response = devisService.creerDevisAvenant(id, userDetails.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Signe un contrat avec le code OTP.
     */
    @PostMapping("/{id}/signer")
    public ResponseEntity<ContratResponse> signerContrat(
            @PathVariable Long id,
            @Valid @RequestBody SignatureRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ContratResponse response = contratService.signerContrat(id, userDetails.getUser().getId(), request.getCode());
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère le PDF du contrat.
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        byte[] pdfContent = contratService.getPdfContent(id, userDetails.getUser().getId());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "inline; filename=\"contrat.pdf\"")
                .body(pdfContent);
    }

    /**
     * Statistiques des contrats pour le dashboard.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        List<ContratResponse> contrats = contratService.listerContrats(userId);

        long actifs = contrats.stream().filter(c -> c.getStatut() == StatutContrat.ACTIF).count();
        long enAttente = contrats.stream().filter(c -> c.getStatut() == StatutContrat.EN_ATTENTE).count();

        return ResponseEntity.ok(Map.of(
                "actifs", actifs,
                "enAttente", enAttente,
                "total", (long) contrats.size()
        ));
    }
}
