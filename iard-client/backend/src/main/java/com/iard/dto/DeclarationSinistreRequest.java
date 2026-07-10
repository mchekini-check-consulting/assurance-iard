package com.iard.dto;

import com.iard.entity.TypeSinistre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeclarationSinistreRequest {

    @NotNull(message = "Le contrat concerné est obligatoire")
    private Long contratId;

    @NotNull(message = "Le type de sinistre est obligatoire")
    private TypeSinistre type;

    @NotNull(message = "La date du sinistre est obligatoire")
    @PastOrPresent(message = "La date du sinistre ne peut pas être dans le futur")
    private LocalDate dateSinistre;

    @NotBlank(message = "Le lieu du sinistre est obligatoire")
    private String lieu;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 50, message = "La description doit comporter au moins 50 caractères")
    private String description;

    private BigDecimal montantEstime;
}
