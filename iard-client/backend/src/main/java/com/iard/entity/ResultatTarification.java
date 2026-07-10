package com.iard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultatTarification implements Serializable {

    private Formule formule;
    private BigDecimal primeHT;
    private BigDecimal taxes;
    private BigDecimal primeTTC;
    private BigDecimal primeMensuelle;
    private List<GarantieDetail> garantiesIncluses;
    private List<GarantieDetail> garantiesOptionnelles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GarantieDetail implements Serializable {
        private String code;
        private String libelle;
        private BigDecimal plafond;
        private BigDecimal franchise;
        private boolean incluse;
        private BigDecimal primeSupplementaire;
    }
}
