package com.iard.service;

import com.iard.dto.TarificationRequest;
import com.iard.entity.Formule;
import com.iard.entity.ResultatTarification;
import com.iard.entity.TypeBien;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class TarificationService {

    /**
     * Mock de tarification habitation.
     * Calcule une prime basée sur les caractéristiques du bien.
     */
    public ResultatTarification calculerTarif(TarificationRequest request) {
        // Base de calcul selon la surface
        BigDecimal primeBase = BigDecimal.valueOf(request.getSurfaceHabitable())
                .multiply(BigDecimal.valueOf(2.5));

        // Coefficient selon le type de bien
        if (request.getTypeBien() == TypeBien.MAISON) {
            primeBase = primeBase.multiply(BigDecimal.valueOf(1.2));
        }

        // Coefficient selon la formule
        BigDecimal coeffFormule = switch (request.getFormule()) {
            case ESSENTIELLE -> BigDecimal.valueOf(1.0);
            case CONFORT -> BigDecimal.valueOf(1.35);
            case PREMIUM -> BigDecimal.valueOf(1.7);
        };
        primeBase = primeBase.multiply(coeffFormule);

        // Réductions sécurité
        if (Boolean.TRUE.equals(request.getAlarme())) {
            primeBase = primeBase.multiply(BigDecimal.valueOf(0.95));
        }
        if (Boolean.TRUE.equals(request.getPorteBlindee())) {
            primeBase = primeBase.multiply(BigDecimal.valueOf(0.97));
        }

        // Majoration sinistres
        if (request.getNombreSinistres36Mois() != null && request.getNombreSinistres36Mois() > 0) {
            BigDecimal majoration = BigDecimal.valueOf(1 + (request.getNombreSinistres36Mois() * 0.1));
            primeBase = primeBase.multiply(majoration);
        }

        // Majoration piscine/dépendances
        if (Boolean.TRUE.equals(request.getPiscine())) {
            primeBase = primeBase.add(BigDecimal.valueOf(50));
        }
        if (Boolean.TRUE.equals(request.getDependances())) {
            primeBase = primeBase.add(BigDecimal.valueOf(30));
        }

        // Capital mobilier
        if (request.getCapitalMobilier() != null) {
            BigDecimal tauxMobilier = request.getCapitalMobilier()
                    .divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);
            primeBase = primeBase.add(tauxMobilier.multiply(BigDecimal.valueOf(15)));
        }

        // Objets de valeur
        if (Boolean.TRUE.equals(request.getObjetsValeur()) && request.getValeurObjetsValeur() != null) {
            BigDecimal tauxObjets = request.getValeurObjetsValeur()
                    .divide(BigDecimal.valueOf(5000), 2, RoundingMode.HALF_UP);
            primeBase = primeBase.add(tauxObjets.multiply(BigDecimal.valueOf(25)));
        }

        // Options supplémentaires
        BigDecimal primeOptions = BigDecimal.ZERO;
        List<ResultatTarification.GarantieDetail> garantiesOptionnelles = new ArrayList<>();

        if (request.getOptionsGaranties() != null) {
            for (String option : request.getOptionsGaranties()) {
                BigDecimal primeOption = calculerPrimeOption(option);
                primeOptions = primeOptions.add(primeOption);
                garantiesOptionnelles.add(createGarantieOptionnelle(option, primeOption));
            }
        }

        BigDecimal primeHT = primeBase.add(primeOptions).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxes = primeHT.multiply(BigDecimal.valueOf(0.30)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal primeTTC = primeHT.add(taxes).setScale(2, RoundingMode.HALF_UP);
        BigDecimal primeMensuelle = primeTTC.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        return ResultatTarification.builder()
                .formule(request.getFormule())
                .primeHT(primeHT)
                .taxes(taxes)
                .primeTTC(primeTTC)
                .primeMensuelle(primeMensuelle)
                .garantiesIncluses(getGarantiesIncluses(request.getFormule()))
                .garantiesOptionnelles(garantiesOptionnelles)
                .build();
    }

    private BigDecimal calculerPrimeOption(String option) {
        return switch (option) {
            case "BRIS_GLACE" -> BigDecimal.valueOf(24);
            case "VOL_HORS_DOMICILE" -> BigDecimal.valueOf(36);
            case "JARDIN" -> BigDecimal.valueOf(18);
            case "PISCINE_PLUS" -> BigDecimal.valueOf(45);
            case "DOMMAGES_ELECTRIQUES" -> BigDecimal.valueOf(30);
            case "ASSISTANCE_PLUS" -> BigDecimal.valueOf(42);
            default -> BigDecimal.ZERO;
        };
    }

    private ResultatTarification.GarantieDetail createGarantieOptionnelle(String code, BigDecimal prime) {
        String libelle = switch (code) {
            case "BRIS_GLACE" -> "Bris de glace";
            case "VOL_HORS_DOMICILE" -> "Vol hors domicile";
            case "JARDIN" -> "Protection jardin";
            case "PISCINE_PLUS" -> "Piscine Plus";
            case "DOMMAGES_ELECTRIQUES" -> "Dommages électriques";
            case "ASSISTANCE_PLUS" -> "Assistance Plus 24/7";
            default -> code;
        };

        return ResultatTarification.GarantieDetail.builder()
                .code(code)
                .libelle(libelle)
                .incluse(false)
                .primeSupplementaire(prime)
                .build();
    }

    private List<ResultatTarification.GarantieDetail> getGarantiesIncluses(Formule formule) {
        List<ResultatTarification.GarantieDetail> garanties = new ArrayList<>();

        // Garanties de base (toutes formules)
        garanties.add(createGarantieIncluse("INCENDIE", "Incendie et explosion",
                BigDecimal.valueOf(500000), BigDecimal.valueOf(150)));
        garanties.add(createGarantieIncluse("DEGATS_EAUX", "Dégâts des eaux",
                BigDecimal.valueOf(300000), BigDecimal.valueOf(200)));
        garanties.add(createGarantieIncluse("CATASTROPHES_NATURELLES", "Catastrophes naturelles",
                BigDecimal.valueOf(500000), BigDecimal.valueOf(380)));
        garanties.add(createGarantieIncluse("RC_VIE_PRIVEE", "Responsabilité civile vie privée",
                BigDecimal.valueOf(3000000), BigDecimal.ZERO));

        if (formule == Formule.CONFORT || formule == Formule.PREMIUM) {
            garanties.add(createGarantieIncluse("VOL", "Vol et vandalisme",
                    BigDecimal.valueOf(50000), BigDecimal.valueOf(300)));
            garanties.add(createGarantieIncluse("BRIS_GLACE", "Bris de glace",
                    BigDecimal.valueOf(10000), BigDecimal.valueOf(75)));
        }

        if (formule == Formule.PREMIUM) {
            garanties.add(createGarantieIncluse("DOMMAGES_ELECTRIQUES", "Dommages électriques",
                    BigDecimal.valueOf(8000), BigDecimal.valueOf(100)));
            garanties.add(createGarantieIncluse("PERTE_LOYERS", "Perte de loyers",
                    BigDecimal.valueOf(12000), BigDecimal.ZERO));
            garanties.add(createGarantieIncluse("ASSISTANCE_24H", "Assistance 24h/24",
                    null, BigDecimal.ZERO));
        }

        return garanties;
    }

    private ResultatTarification.GarantieDetail createGarantieIncluse(String code, String libelle,
                                                                       BigDecimal plafond, BigDecimal franchise) {
        return ResultatTarification.GarantieDetail.builder()
                .code(code)
                .libelle(libelle)
                .plafond(plafond)
                .franchise(franchise)
                .incluse(true)
                .primeSupplementaire(BigDecimal.ZERO)
                .build();
    }
}
