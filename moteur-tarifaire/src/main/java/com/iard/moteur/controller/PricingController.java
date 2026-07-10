package com.iard.moteur.controller;

import com.iard.moteur.dto.HabitationRequest;
import com.iard.moteur.dto.TarificationResponse;
import com.iard.moteur.service.TarificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final TarificationService tarificationService;

    public PricingController(TarificationService tarificationService) {
        this.tarificationService = tarificationService;
    }

    @PostMapping("/habitation")
    public ResponseEntity<TarificationResponse> calculerTarification(
            @Valid @RequestBody HabitationRequest request) {
        TarificationResponse response = tarificationService.calculerTarification(request);
        return ResponseEntity.ok(response);
    }
}
