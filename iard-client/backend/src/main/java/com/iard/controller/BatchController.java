package com.iard.controller;

import com.iard.service.PrelevementBatchService;
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
@RequiredArgsConstructor
public class BatchController {

    private final PrelevementBatchService prelevementBatchService;

    /**
     * Déclenche manuellement le batch de prélèvements.
     *
     * @param date Date de prélèvement (optionnel, défaut = aujourd'hui)
     * @return Résultat du batch
     */
    @PostMapping("/prelevements")
    public ResponseEntity<Map<String, Object>> executerPrelevements(
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
