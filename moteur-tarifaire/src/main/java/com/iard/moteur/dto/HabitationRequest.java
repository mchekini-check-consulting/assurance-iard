package com.iard.moteur.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class HabitationRequest {

    @NotNull(message = "Le type d'habitation est requis")
    private TypeHabitation typeHabitation;

    @NotNull(message = "La surface habitable est requise")
    @Min(value = 1, message = "La surface doit être supérieure à 0")
    private Integer surfaceHabitable;

    @NotNull(message = "Le nombre de pièces est requis")
    @Min(value = 1, message = "Le nombre de pièces doit être au moins 1")
    private Integer nombrePieces;

    @NotNull(message = "Le statut d'occupation est requis")
    private StatutOccupation statutOccupation;

    @NotBlank(message = "Le code postal est requis")
    @Pattern(regexp = "^[0-9]{5}$", message = "Le code postal doit contenir 5 chiffres")
    private String codePostal;

    @NotNull(message = "Le nombre de sinistres est requis")
    @Min(value = 0, message = "Le nombre de sinistres ne peut pas être négatif")
    private Integer nombreSinistres36Mois;

    private boolean alarme;

    private boolean porteBlindee;

    @NotNull(message = "Le capital mobilier est requis")
    @DecimalMin(value = "0", message = "Le capital mobilier ne peut pas être négatif")
    private BigDecimal capitalMobilier;

    @NotNull(message = "La formule est requise")
    private Formule formule;

    private List<GarantieOptionnelle> garantiesOptionnelles;

    // Getters and Setters
    public TypeHabitation getTypeHabitation() {
        return typeHabitation;
    }

    public void setTypeHabitation(TypeHabitation typeHabitation) {
        this.typeHabitation = typeHabitation;
    }

    public Integer getSurfaceHabitable() {
        return surfaceHabitable;
    }

    public void setSurfaceHabitable(Integer surfaceHabitable) {
        this.surfaceHabitable = surfaceHabitable;
    }

    public Integer getNombrePieces() {
        return nombrePieces;
    }

    public void setNombrePieces(Integer nombrePieces) {
        this.nombrePieces = nombrePieces;
    }

    public StatutOccupation getStatutOccupation() {
        return statutOccupation;
    }

    public void setStatutOccupation(StatutOccupation statutOccupation) {
        this.statutOccupation = statutOccupation;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public Integer getNombreSinistres36Mois() {
        return nombreSinistres36Mois;
    }

    public void setNombreSinistres36Mois(Integer nombreSinistres36Mois) {
        this.nombreSinistres36Mois = nombreSinistres36Mois;
    }

    public boolean isAlarme() {
        return alarme;
    }

    public void setAlarme(boolean alarme) {
        this.alarme = alarme;
    }

    public boolean isPorteBlindee() {
        return porteBlindee;
    }

    public void setPorteBlindee(boolean porteBlindee) {
        this.porteBlindee = porteBlindee;
    }

    public BigDecimal getCapitalMobilier() {
        return capitalMobilier;
    }

    public void setCapitalMobilier(BigDecimal capitalMobilier) {
        this.capitalMobilier = capitalMobilier;
    }

    public Formule getFormule() {
        return formule;
    }

    public void setFormule(Formule formule) {
        this.formule = formule;
    }

    public List<GarantieOptionnelle> getGarantiesOptionnelles() {
        return garantiesOptionnelles;
    }

    public void setGarantiesOptionnelles(List<GarantieOptionnelle> garantiesOptionnelles) {
        this.garantiesOptionnelles = garantiesOptionnelles;
    }

    public enum TypeHabitation {
        APPARTEMENT, MAISON
    }

    public enum StatutOccupation {
        PROPRIETAIRE_OCCUPANT, LOCATAIRE, PROPRIETAIRE_NON_OCCUPANT
    }

    public enum Formule {
        ESSENTIELLE, CONFORT, PREMIUM
    }

    public enum GarantieOptionnelle {
        PISCINE, DEPENDANCES, OBJETS_VALEUR, EQUIPEMENT_JARDIN
    }
}
