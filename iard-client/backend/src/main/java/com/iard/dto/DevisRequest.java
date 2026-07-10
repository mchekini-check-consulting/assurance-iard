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
public class DevisRequest {

    private Integer etapeCourante;

    // Étape 1 : Le bien
    private TypeBien typeBien;
    private TypeResidence typeResidence;
    private String adresse;
    private String codePostal;
    private String ville;
    private Integer surfaceHabitable;
    private Integer nombrePieces;
    private Integer etage;
    private Integer anneeConstruction;

    // Étape 2 : L'occupation
    private StatutOccupation statutOccupation;

    // Étape 3 : Sécurité & spécificités
    private Boolean alarme;
    private Boolean porteBlindee;
    private Boolean dependances;
    private Integer surfaceDependances;
    private Boolean piscine;

    // Étape 4 : Le contenu
    private BigDecimal capitalMobilier;
    private Boolean objetsValeur;
    private BigDecimal valeurObjetsValeur;

    // Étape 5 : Antécédents
    private Integer nombreSinistres36Mois;

    // Étape 6 : Formule & options
    private Formule formule;
    private List<String> optionsGaranties;

    // Assuré (si différent du souscripteur)
    private Boolean souscripteurEstAssure;
    private PersonneAssureeDto assure;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonneAssureeDto {
        private Civilite civilite;
        private String prenom;
        private String nom;
        private String email;
        private String telephone;
        private String adresse;
        private String codePostal;
        private String ville;
    }
}
