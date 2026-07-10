package com.iard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TitreSejourExtraction {
    private String nom;
    private String prenom;
    private String numero;
    private String dateExpiration; // Format YYYY-MM-DD
    private boolean extractionReussie;
    private String erreur;
}
