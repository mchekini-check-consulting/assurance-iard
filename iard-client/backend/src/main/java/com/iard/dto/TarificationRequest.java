package com.iard.dto;

import com.iard.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarificationRequest {

    // Le bien
    private TypeBien typeBien;
    private TypeResidence typeResidence;
    private String codePostal;
    private Integer surfaceHabitable;
    private Integer nombrePieces;
    private Integer etage;
    private Integer anneeConstruction;

    // L'occupation
    private StatutOccupation statutOccupation;

    // Sécurité & spécificités
    private Boolean alarme;
    private Boolean porteBlindee;
    private Boolean dependances;
    private Integer surfaceDependances;
    private Boolean piscine;

    // Le contenu
    private BigDecimal capitalMobilier;
    private Boolean objetsValeur;
    private BigDecimal valeurObjetsValeur;

    // Antécédents
    private Integer nombreSinistres36Mois;

    // Formule & options
    private Formule formule;
    private List<String> optionsGaranties;
}
