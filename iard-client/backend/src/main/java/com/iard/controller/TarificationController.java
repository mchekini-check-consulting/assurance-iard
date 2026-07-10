package com.iard.controller;

import com.iard.dto.TarificationRequest;
import com.iard.entity.ResultatTarification;
import com.iard.service.TarificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
@Tag(name = "Tarification", description = "Calcul de prime habitation à partir des caractéristiques du bien "
        + "(moteur de tarification mocké embarqué).")
public class TarificationController {

    private final TarificationService tarificationService;

    @Operation(summary = "Calculer un tarif habitation",
            description = "Calcule la prime (HT, taxes, TTC, mensualité) et le détail des garanties à partir "
                    + "des caractéristiques du bien : surface, type, formule (ESSENTIELLE/CONFORT/PREMIUM), "
                    + "équipements de sécurité, sinistralité et options.")
    @PostMapping("/habitation")
    public ResponseEntity<ResultatTarification> calculerTarifHabitation(
            @RequestBody TarificationRequest request) {
        ResultatTarification resultat = tarificationService.calculerTarif(request);
        return ResponseEntity.ok(resultat);
    }
}
