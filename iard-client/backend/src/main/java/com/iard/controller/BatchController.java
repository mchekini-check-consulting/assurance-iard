package com.iard.controller;

import com.iard.service.PrelevementBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Controller pour le déclenchement manuel des batchs.
 * Utilisé principalement pour les tests.
 */
@Slf4j
@RestController
@RequestMapping("/api/batch")
@Tag(name = "Batch", description = "Déclenchement manuel des traitements planifiés (pour les tests et démos).")
@RequiredArgsConstructor
public class BatchController {

    private final PrelevementBatchService prelevementBatchService;

    /**
     * Déclenche manuellement le batch de prélèvements.
     *
     * @param date Date de prélèvement (optionnel, défaut = aujourd'hui)
     * @return Résultat du batch
     */
    @Operation(summary = "Exécuter le batch de prélèvements",
            description = "Lance immédiatement le batch quotidien (planifié à 6h00) : prélève les contrats dont "
                    + "l'échéance est atteinte, génère les factures et active les contrats en attente. "
                    + "Rappel du mock de paiement : un montant mensuel supérieur à 30 € échoue volontairement.")
    @PostMapping("/prelevements")
    public ResponseEntity<Map<String, Object>> executerPrelevements(
            @Parameter(description = "Date de prélèvement à simuler (défaut : aujourd'hui)", example = "2026-07-10")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate datePrelevement = date != null ? date : LocalDate.now();
        log.info("Déclenchement manuel du batch de prélèvements pour la date: {}", datePrelevement);

        PrelevementBatchService.BatchResult result = prelevementBatchService.executerPrelevements(datePrelevement);

        return ResponseEntity.ok(Map.of(
                "date", datePrelevement.toString(),
                "total", result.total(),
                "succes", result.succes(),
                "echecs", result.echecs(),
                "message", String.format("Batch exécuté: %d/%d succès", result.succes(), result.total())
        ));
    }
}
