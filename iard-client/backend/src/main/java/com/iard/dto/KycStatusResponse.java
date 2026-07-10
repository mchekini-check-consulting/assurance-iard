package com.iard.dto;

import com.iard.entity.DonneesExtraitesKyc;
import com.iard.entity.StatutKyc;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycStatusResponse {
    private Long id;
    private StatutKyc statut;
    private DonneesExtraitesKyc donneesExtraites;
    private LocalDateTime dateVerification;
    private String motifRefus;
    private boolean titreSejour_uploaded;
    private boolean rib_uploaded;
}
