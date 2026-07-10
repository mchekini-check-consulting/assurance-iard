package com.iard.dto;

import com.iard.entity.DonneesExtraitesKyc;
import com.iard.entity.StatutKyc;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycVerificationResult {
    private StatutKyc statut;
    private DonneesExtraitesKyc donneesExtraites;
    private boolean success;
    private List<String> erreurs;
    private String message;
}
