package com.iard.controller;

import com.iard.dto.AvenantRequest;
import com.iard.dto.ContratResponse;
import com.iard.dto.DevisResponse;
import com.iard.dto.SignatureRequest;
import com.iard.entity.StatutContrat;
import com.iard.security.UserDetailsImpl;
import com.iard.service.ContratService;
import com.iard.service.DevisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Contrats", description = "Cycle de vie des contrats : génération depuis un devis, signature "
        + "électronique, avenant selfcare, PDF et statistiques. Authentification requise.")
public class ContratController {

    private final ContratService contratService;
    private final DevisService devisService;

    /**
     * Génère un contrat à partir d'un devis.
     */
    @Operation(summary = "Générer un contrat depuis un devis",
            description = "Transforme un devis tarifé (statut DEVIS ou PROPOSITION) en contrat EN_ATTENTE de "
                    + "signature. Le KYC de l'utilisateur doit être vérifié (sinon erreur KYC_REQUIRED). "
                    + "Idempotent : si un contrat existe déjà pour ce devis, il est retourné.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrat créé (ou existant) retourné"),
            @ApiResponse(responseCode = "400", description = "Devis introuvable, non finalisé ou KYC non vérifié")
    })
    @PostMapping("/generer/{devisId}")
    public ResponseEntity<ContratResponse> genererContrat(
            @Parameter(description = "Identifiant du devis tarifé à transformer en contrat", example = "42")
            @PathVariable Long devisId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ContratResponse response = contratService.genererContrat(devisId, userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Liste tous les contrats de l'utilisateur.
     */
    @Operation(summary = "Lister mes contrats",
            description = "Retourne tous les contrats de l'utilisateur connecté, du plus récent au plus ancien, "
                    + "avec leurs garanties, primes et données du bien assuré.")
    @GetMapping
    public ResponseEntity<List<ContratResponse>> listerContrats(
            @Parameter(description = "Filtre optionnel par statut du contrat", example = "ACTIF")
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
    @Operation(summary = "Consulter un contrat",
            description = "Retourne le détail d'un contrat appartenant à l'utilisateur connecté.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrat retourné"),
            @ApiResponse(responseCode = "400", description = "Contrat introuvable ou n'appartenant pas à l'utilisateur")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContratResponse> getContrat(
            @Parameter(description = "Identifiant du contrat", example = "1")
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
    @Operation(summary = "Modifier les garanties d'un contrat (avenant selfcare)",
            description = "Crée un devis d'avenant à partir d'un contrat ACTIF : les données du risque (adresse, "
                    + "surface, occupation…) sont reprises du contrat, les garanties passées dans la requête sont "
                    + "appliquées et le tarif est recalculé. Le devis retourné suit ensuite le parcours habituel "
                    + "(génération de contrat puis signature) ; à la signature du nouveau contrat, le contrat "
                    + "d'origine est automatiquement résilié.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devis d'avenant tarifé retourné"),
            @ApiResponse(responseCode = "400", description = "Contrat introuvable ou non actif")
    })
    @PostMapping("/{id}/avenant")
    public ResponseEntity<DevisResponse> creerAvenant(
            @Parameter(description = "Identifiant du contrat ACTIF dont on modifie les garanties", example = "1")
            @PathVariable Long id,
            @RequestBody AvenantRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DevisResponse response = devisService.creerDevisAvenant(id, userDetails.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Signe un contrat avec le code OTP.
     */
    @Operation(summary = "Signer un contrat",
            description = "Signature électronique d'un contrat EN_ATTENTE avec le code OTP reçu par SMS "
                    + "(mock : le code attendu est toujours 6208). Le premier prélèvement est simulé "
                    + "immédiatement et le contrat passe à ACTIF. Si le contrat provient d'un avenant, "
                    + "le contrat d'origine est résilié dans la même transaction.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrat signé et activé"),
            @ApiResponse(responseCode = "400", description = "Code invalide, contrat introuvable ou déjà signé")
    })
    @PostMapping("/{id}/signer")
    public ResponseEntity<ContratResponse> signerContrat(
            @Parameter(description = "Identifiant du contrat à signer", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody SignatureRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ContratResponse response = contratService.signerContrat(id, userDetails.getUser().getId(), request.getCode());
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère le PDF du contrat.
     */
    @Operation(summary = "Télécharger le PDF du contrat",
            description = "Retourne le PDF du contrat (avec cachet de signature s'il est signé). "
                    + "Si le fichier n'existe plus sur le serveur, il est régénéré à la volée depuis la base.")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(
            @Parameter(description = "Identifiant du contrat", example = "1")
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
    @Operation(summary = "Statistiques de mes contrats",
            description = "Compteurs pour le tableau de bord : contrats actifs, en attente de signature et total.")
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
