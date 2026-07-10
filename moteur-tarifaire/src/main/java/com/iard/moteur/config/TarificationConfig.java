package com.iard.moteur.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "tarification")
public class TarificationConfig {

    private Base base;
    private Coefficients coefficients;
    private BigDecimal tauxCapitalMobilier;
    private Map<String, BigDecimal> forfaits;
    private Map<String, BigDecimal> garantiesOptionnelles;
    private BigDecimal tauxTsca;
    private BigDecimal contributionFondsGarantie;
    private Map<String, List<Integer>> zonesCodePostal;

    public Base getBase() {
        return base;
    }

    public void setBase(Base base) {
        this.base = base;
    }

    public Coefficients getCoefficients() {
        return coefficients;
    }

    public void setCoefficients(Coefficients coefficients) {
        this.coefficients = coefficients;
    }

    public BigDecimal getTauxCapitalMobilier() {
        return tauxCapitalMobilier;
    }

    public void setTauxCapitalMobilier(BigDecimal tauxCapitalMobilier) {
        this.tauxCapitalMobilier = tauxCapitalMobilier;
    }

    public Map<String, BigDecimal> getForfaits() {
        return forfaits;
    }

    public void setForfaits(Map<String, BigDecimal> forfaits) {
        this.forfaits = forfaits;
    }

    public Map<String, BigDecimal> getGarantiesOptionnelles() {
        return garantiesOptionnelles;
    }

    public void setGarantiesOptionnelles(Map<String, BigDecimal> garantiesOptionnelles) {
        this.garantiesOptionnelles = garantiesOptionnelles;
    }

    public BigDecimal getTauxTsca() {
        return tauxTsca;
    }

    public void setTauxTsca(BigDecimal tauxTsca) {
        this.tauxTsca = tauxTsca;
    }

    public BigDecimal getContributionFondsGarantie() {
        return contributionFondsGarantie;
    }

    public void setContributionFondsGarantie(BigDecimal contributionFondsGarantie) {
        this.contributionFondsGarantie = contributionFondsGarantie;
    }

    public Map<String, List<Integer>> getZonesCodePostal() {
        return zonesCodePostal;
    }

    public void setZonesCodePostal(Map<String, List<Integer>> zonesCodePostal) {
        this.zonesCodePostal = zonesCodePostal;
    }

    public static class Base {
        private BigDecimal appartement;
        private BigDecimal maison;

        public BigDecimal getAppartement() {
            return appartement;
        }

        public void setAppartement(BigDecimal appartement) {
            this.appartement = appartement;
        }

        public BigDecimal getMaison() {
            return maison;
        }

        public void setMaison(BigDecimal maison) {
            this.maison = maison;
        }
    }

    public static class Coefficients {
        private Map<String, BigDecimal> statutOccupation;
        private Map<String, BigDecimal> zone;
        private Map<String, BigDecimal> sinistralite;
        private BigDecimal piecesSupplementaires;
        private BigDecimal reductionSecurite;

        public Map<String, BigDecimal> getStatutOccupation() {
            return statutOccupation;
        }

        public void setStatutOccupation(Map<String, BigDecimal> statutOccupation) {
            this.statutOccupation = statutOccupation;
        }

        public Map<String, BigDecimal> getZone() {
            return zone;
        }

        public void setZone(Map<String, BigDecimal> zone) {
            this.zone = zone;
        }

        public Map<String, BigDecimal> getSinistralite() {
            return sinistralite;
        }

        public void setSinistralite(Map<String, BigDecimal> sinistralite) {
            this.sinistralite = sinistralite;
        }

        public BigDecimal getPiecesSupplementaires() {
            return piecesSupplementaires;
        }

        public void setPiecesSupplementaires(BigDecimal piecesSupplementaires) {
            this.piecesSupplementaires = piecesSupplementaires;
        }

        public BigDecimal getReductionSecurite() {
            return reductionSecurite;
        }

        public void setReductionSecurite(BigDecimal reductionSecurite) {
            this.reductionSecurite = reductionSecurite;
        }
    }
}
