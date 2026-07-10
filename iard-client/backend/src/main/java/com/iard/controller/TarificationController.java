package com.iard.controller;

import com.iard.dto.TarificationRequest;
import com.iard.entity.ResultatTarification;
import com.iard.service.TarificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class TarificationController {

    private final TarificationService tarificationService;

    @PostMapping("/habitation")
    public ResponseEntity<ResultatTarification> calculerTarifHabitation(
            @RequestBody TarificationRequest request) {
        ResultatTarification resultat = tarificationService.calculerTarif(request);
        return ResponseEntity.ok(resultat);
    }
}
