package com.iard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureResponse {
    private Long id;
    private String numeroFacture;
    private Long contratId;
    private String numeroContrat;
    private String produit;
    private BigDecimal montantHT;
    private BigDecimal taxes;
    private BigDecimal montantTTC;
    private String periode;
    private LocalDate dateEmission;
    private LocalDate datePaiement;
    private String pdfUrl;
    private LocalDateTime createdAt;
}
