package com.iard.moteur.service;

import com.iard.moteur.config.TarificationConfig;
import com.iard.moteur.dto.GarantieDetail;
import com.iard.moteur.dto.HabitationRequest;
import com.iard.moteur.dto.TarificationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class TarificationService {

    private final TarificationConfig config;

    public TarificationService(TarificationConfig config) {
        this.config = config;
    }

    public TarificationResponse calculerTarification(HabitationRequest request) {
        // 1. Prime de base selon le type et la surface
        BigDecimal primeBase = calculerPrimeBase(request);

        // 2. Application des coefficients multiplicateurs
        BigDecimal coefficientTotal = calculerCoefficientTotal(request);
        BigDecimal primeApresCoefficients = primeBase.multiply(coefficientTotal);

        // 3. Capital mobilier
        BigDecimal primeCapitalMobilier = request.getCapitalMobilier()
                .multiply(config.getTauxCapitalMobilier());

        // 4. Forfait formule
        BigDecimal forfaitFormule = getForfaitFormule(request.getFormule());

        // 5. Garanties optionnelles
        BigDecimal totalGarantiesOptionnelles = calculerGarantiesOptionnelles(request);

        // Calcul de la prime HT
        BigDecimal primeHT = primeApresCoefficients
                .add(primeCapitalMobilier)
                .add(forfaitFormule)
                .add(totalGarantiesOptionnelles)
                .setScale(2, RoundingMode.HALF_UP);

        // 6. Taxes
        BigDecimal taxes = primeHT.multiply(config.getTauxTsca())
                .setScale(2, RoundingMode.HALF_UP);

        // 7. Prime TTC
        BigDecimal primeTTC = primeHT
                .multiply(BigDecimal.ONE.add(config.getTauxTsca()))
                .add(config.getContributionFondsGarantie())
                .setScale(2, RoundingMode.HALF_UP);

        // Prime mensuelle
        BigDecimal primeMensuelleTTC = primeTTC
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        // Construction du détail des garanties
        List<GarantieDetail> garanties = construireDetailGaranties(request, primeApresCoefficients);

        return TarificationResponse.builder()
                .formule(formatFormule(request.getFormule()))
                .periodicite("annuelle")
                .primeHT(primeHT)
                .taxes(taxes)
                .primeTTC(primeTTC)
                .primeMensuelleTTC(primeMensuelleTTC)
                .garanties(garanties)
                .build();
    }

    private BigDecimal calculerPrimeBase(HabitationRequest request) {
        BigDecimal baseParM2 = switch (request.getTypeHabitation()) {
            case APPARTEMENT -> config.getBase().getAppartement();
            case MAISON -> config.getBase().getMaison();
        };
        return baseParM2.multiply(BigDecimal.valueOf(request.getSurfaceHabitable()));
    }

    private BigDecimal calculerCoefficientTotal(HabitationRequest request) {
        BigDecimal coefficient = BigDecimal.ONE;

        // Coefficient statut d'occupation
        String keyStatut = switch (request.getStatutOccupation()) {
            case PROPRIETAIRE_OCCUPANT -> "proprietaire-occupant";
            case LOCATAIRE -> "locataire";
            case PROPRIETAIRE_NON_OCCUPANT -> "proprietaire-non-occupant";
        };
        coefficient = coefficient.multiply(config.getCoefficients().getStatutOccupation().get(keyStatut));

        // Coefficient zone (basé sur le premier chiffre du code postal)
        String zone = determinerZone(request.getCodePostal());
        coefficient = coefficient.multiply(config.getCoefficients().getZone().get(zone));

        // Coefficient nombre de pièces (+5% par pièce au-delà de 3)
        if (request.getNombrePieces() > 3) {
            int piecesSupplementaires = request.getNombrePieces() - 3;
            BigDecimal coeffPieces = BigDecimal.ONE.add(
                    config.getCoefficients().getPiecesSupplementaires()
                            .multiply(BigDecimal.valueOf(piecesSupplementaires))
            );
            coefficient = coefficient.multiply(coeffPieces);
        }

        // Coefficient sinistralité
        String keySinistralite = switch (request.getNombreSinistres36Mois()) {
            case 0 -> "zero";
            case 1 -> "un";
            default -> "deux-plus";
        };
        coefficient = coefficient.multiply(config.getCoefficients().getSinistralite().get(keySinistralite));

        // Réduction sécurité (-10% si alarme + porte blindée)
        if (request.isAlarme() && request.isPorteBlindee()) {
            coefficient = coefficient.multiply(
                    BigDecimal.ONE.subtract(config.getCoefficients().getReductionSecurite())
            );
        }

        return coefficient;
    }

    private String determinerZone(String codePostal) {
        int premierChiffre = Character.getNumericValue(codePostal.charAt(0));

        for (var entry : config.getZonesCodePostal().entrySet()) {
            if (entry.getValue().contains(premierChiffre)) {
                return entry.getKey();
            }
        }
        return "moyenne"; // Par défaut
    }

    private BigDecimal getForfaitFormule(HabitationRequest.Formule formule) {
        String key = formule.name().toLowerCase();
        return config.getForfaits().getOrDefault(key, BigDecimal.ZERO);
    }

    private BigDecimal calculerGarantiesOptionnelles(HabitationRequest request) {
        if (request.getGarantiesOptionnelles() == null || request.getGarantiesOptionnelles().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (HabitationRequest.GarantieOptionnelle garantie : request.getGarantiesOptionnelles()) {
            String key = switch (garantie) {
                case PISCINE -> "piscine";
                case DEPENDANCES -> "dependances";
                case OBJETS_VALEUR -> "objets-valeur";
                case EQUIPEMENT_JARDIN -> "equipement-jardin";
            };
            total = total.add(config.getGarantiesOptionnelles().getOrDefault(key, BigDecimal.ZERO));
        }
        return total;
    }

    private List<GarantieDetail> construireDetailGaranties(HabitationRequest request, BigDecimal primeBase) {
        List<GarantieDetail> garanties = new ArrayList<>();

        // Répartition indicative de la prime de base entre les garanties incluses
        BigDecimal partIncendie = primeBase.multiply(BigDecimal.valueOf(0.25));
        BigDecimal partDDE = primeBase.multiply(BigDecimal.valueOf(0.22));
        BigDecimal partRC = primeBase.multiply(BigDecimal.valueOf(0.15));
        BigDecimal partCatNat = primeBase.multiply(BigDecimal.valueOf(0.12));
        BigDecimal partVol = primeBase.multiply(BigDecimal.valueOf(0.16));
        BigDecimal partBDG = primeBase.multiply(BigDecimal.valueOf(0.10));

        // Garanties Essentielle (toujours incluses)
        garanties.add(GarantieDetail.builder()
                .code("INCENDIE")
                .libelle("Incendie & explosions")
                .incluse(true)
                .montantHT(partIncendie.setScale(2, RoundingMode.HALF_UP))
                .build());

        garanties.add(GarantieDetail.builder()
                .code("DDE")
                .libelle("Dégâts des eaux")
                .incluse(true)
                .montantHT(partDDE.setScale(2, RoundingMode.HALF_UP))
                .build());

        garanties.add(GarantieDetail.builder()
                .code("RC")
                .libelle("Responsabilité civile")
                .incluse(true)
                .montantHT(partRC.setScale(2, RoundingMode.HALF_UP))
                .build());

        garanties.add(GarantieDetail.builder()
                .code("CATNAT")
                .libelle("Catastrophes naturelles")
                .incluse(true)
                .montantHT(partCatNat.setScale(2, RoundingMode.HALF_UP))
                .build());

        // Garanties Confort et Premium
        boolean isConfortOuPremium = request.getFormule() == HabitationRequest.Formule.CONFORT
                || request.getFormule() == HabitationRequest.Formule.PREMIUM;

        garanties.add(GarantieDetail.builder()
                .code("VOL")
                .libelle("Vol & cambriolage")
                .incluse(isConfortOuPremium)
                .montantHT(isConfortOuPremium ? partVol.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .build());

        garanties.add(GarantieDetail.builder()
                .code("BDG")
                .libelle("Bris de glace")
                .incluse(isConfortOuPremium)
                .montantHT(isConfortOuPremium ? partBDG.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .build());

        // Garanties Premium uniquement
        boolean isPremium = request.getFormule() == HabitationRequest.Formule.PREMIUM;

        garanties.add(GarantieDetail.builder()
                .code("DOMELEC")
                .libelle("Dommages électriques")
                .incluse(isPremium)
                .montantHT(isPremium ? BigDecimal.valueOf(25).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .build());

        garanties.add(GarantieDetail.builder()
                .code("PJ")
                .libelle("Protection juridique")
                .incluse(isPremium)
                .montantHT(isPremium ? BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .build());

        garanties.add(GarantieDetail.builder()
                .code("ASSIST")
                .libelle("Assistance")
                .incluse(isPremium)
                .montantHT(isPremium ? BigDecimal.valueOf(15).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .build());

        // Garanties optionnelles
        if (request.getGarantiesOptionnelles() != null) {
            for (HabitationRequest.GarantieOptionnelle opt : request.getGarantiesOptionnelles()) {
                GarantieDetail detail = switch (opt) {
                    case PISCINE -> GarantieDetail.builder()
                            .code("PISCINE")
                            .libelle("Piscine")
                            .incluse(false)
                            .montantHT(BigDecimal.valueOf(30).setScale(2, RoundingMode.HALF_UP))
                            .build();
                    case DEPENDANCES -> GarantieDetail.builder()
                            .code("DEPEND")
                            .libelle("Dépendances")
                            .incluse(false)
                            .montantHT(BigDecimal.valueOf(25).setScale(2, RoundingMode.HALF_UP))
                            .build();
                    case OBJETS_VALEUR -> GarantieDetail.builder()
                            .code("OBJVAL")
                            .libelle("Objets de valeur")
                            .incluse(false)
                            .montantHT(BigDecimal.valueOf(40).setScale(2, RoundingMode.HALF_UP))
                            .build();
                    case EQUIPEMENT_JARDIN -> GarantieDetail.builder()
                            .code("JARDIN")
                            .libelle("Équipement extérieur/jardin")
                            .incluse(false)
                            .montantHT(BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_UP))
                            .build();
                };
                garanties.add(detail);
            }
        }

        return garanties;
    }

    private String formatFormule(HabitationRequest.Formule formule) {
        return switch (formule) {
            case ESSENTIELLE -> "Essentielle";
            case CONFORT -> "Confort";
            case PREMIUM -> "Premium";
        };
    }
}
