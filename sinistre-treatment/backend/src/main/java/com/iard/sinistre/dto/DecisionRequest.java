package com.iard.sinistre.dto;

import com.iard.sinistre.entity.StatutDossier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionRequest {

    @NotNull(message = "Le nouveau statut est obligatoire")
    private StatutDossier statut;

    private BigDecimal montantRembourse;

    private String commentaire;

    @NotBlank(message = "L'auteur de la décision est obligatoire")
    private String decidePar;
}
