package com.iard.dto;

import com.iard.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevisResponse {

    private Long id;
    private Produit produit;
    private StatutDevis statut;
    private Integer etapeCourante;
    private DonneesRisqueHabitation donneesRisque;
    private PersonneAssuree assure;
    private ResultatTarification resultatTarif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Infos résumées pour la liste
    private String adresseResume;
    private String formuleResume;
    private String primeTTCResume;
}
