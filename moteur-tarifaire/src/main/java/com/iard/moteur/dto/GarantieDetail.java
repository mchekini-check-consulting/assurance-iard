package com.iard.moteur.dto;

import java.math.BigDecimal;

public class GarantieDetail {

    private String code;
    private String libelle;
    private boolean incluse;
    private BigDecimal montantHT;

    public GarantieDetail() {
    }

    public GarantieDetail(String code, String libelle, boolean incluse, BigDecimal montantHT) {
        this.code = code;
        this.libelle = libelle;
        this.incluse = incluse;
        this.montantHT = montantHT;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public boolean isIncluse() {
        return incluse;
    }

    public void setIncluse(boolean incluse) {
        this.incluse = incluse;
    }

    public BigDecimal getMontantHT() {
        return montantHT;
    }

    public void setMontantHT(BigDecimal montantHT) {
        this.montantHT = montantHT;
    }

    public static class Builder {
        private String code;
        private String libelle;
        private boolean incluse;
        private BigDecimal montantHT;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder libelle(String libelle) {
            this.libelle = libelle;
            return this;
        }

        public Builder incluse(boolean incluse) {
            this.incluse = incluse;
            return this;
        }

        public Builder montantHT(BigDecimal montantHT) {
            this.montantHT = montantHT;
            return this;
        }

        public GarantieDetail build() {
            return new GarantieDetail(code, libelle, incluse, montantHT);
        }
    }
}
