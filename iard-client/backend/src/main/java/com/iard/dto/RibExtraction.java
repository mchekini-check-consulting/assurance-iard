package com.iard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RibExtraction {
    private String nom;
    private String prenom;
    private String banque;
    private String iban;
    private boolean extractionReussie;
    private String erreur;
}
