package com.iard.moteur.dto;

import java.math.BigDecimal;
import java.util.List;

public class TarificationResponse {

    private String formule;
    private String periodicite;
    private BigDecimal primeHT;
    private BigDecimal taxes;
    private BigDecimal primeTTC;
    private BigDecimal primeMensuelleTTC;
    private List<GarantieDetail> garanties;

    public TarificationResponse() {
    }

    public TarificationResponse(String formule, String periodicite, BigDecimal primeHT,
                                 BigDecimal taxes, BigDecimal primeTTC,
                                 BigDecimal primeMensuelleTTC, List<GarantieDetail> garanties) {
        this.formule = formule;
        this.periodicite = periodicite;
        this.primeHT = primeHT;
        this.taxes = taxes;
        this.primeTTC = primeTTC;
        this.primeMensuelleTTC = primeMensuelleTTC;
        this.garanties = garanties;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFormule() {
        return formule;
    }

    public void setFormule(String formule) {
        this.formule = formule;
    }

    public String getPeriodicite() {
        return periodicite;
    }

    public void setPeriodicite(String periodicite) {
        this.periodicite = periodicite;
    }

    public BigDecimal getPrimeHT() {
        return primeHT;
    }

    public void setPrimeHT(BigDecimal primeHT) {
        this.primeHT = primeHT;
    }

    public BigDecimal getTaxes() {
        return taxes;
    }

    public void setTaxes(BigDecimal taxes) {
        this.taxes = taxes;
    }

    public BigDecimal getPrimeTTC() {
        return primeTTC;
    }

    public void setPrimeTTC(BigDecimal primeTTC) {
        this.primeTTC = primeTTC;
    }

    public BigDecimal getPrimeMensuelleTTC() {
        return primeMensuelleTTC;
    }

    public void setPrimeMensuelleTTC(BigDecimal primeMensuelleTTC) {
        this.primeMensuelleTTC = primeMensuelleTTC;
    }

    public List<GarantieDetail> getGaranties() {
        return garanties;
    }

    public void setGaranties(List<GarantieDetail> garanties) {
        this.garanties = garanties;
    }

    public static class Builder {
        private String formule;
        private String periodicite;
        private BigDecimal primeHT;
        private BigDecimal taxes;
        private BigDecimal primeTTC;
        private BigDecimal primeMensuelleTTC;
        private List<GarantieDetail> garanties;

        public Builder formule(String formule) {
            this.formule = formule;
            return this;
        }

        public Builder periodicite(String periodicite) {
            this.periodicite = periodicite;
            return this;
        }

        public Builder primeHT(BigDecimal primeHT) {
            this.primeHT = primeHT;
            return this;
        }

        public Builder taxes(BigDecimal taxes) {
            this.taxes = taxes;
            return this;
        }

        public Builder primeTTC(BigDecimal primeTTC) {
            this.primeTTC = primeTTC;
            return this;
        }

        public Builder primeMensuelleTTC(BigDecimal primeMensuelleTTC) {
            this.primeMensuelleTTC = primeMensuelleTTC;
            return this;
        }

        public Builder garanties(List<GarantieDetail> garanties) {
            this.garanties = garanties;
            return this;
        }

        public TarificationResponse build() {
            return new TarificationResponse(formule, periodicite, primeHT, taxes,
                                            primeTTC, primeMensuelleTTC, garanties);
        }
    }
}
