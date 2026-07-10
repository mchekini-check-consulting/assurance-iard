package com.iard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonneesExtraitesKyc implements Serializable {

    // Données extraites du titre de séjour
    private String titreSejour_nom;
    private String titreSejour_prenom;
    private String titreSejour_numero;
    private LocalDate titreSejour_dateExpiration;

    // Données extraites du RIB
    private String rib_nom;
    private String rib_prenom;
    private String rib_banque;
    private String rib_iban;
}
