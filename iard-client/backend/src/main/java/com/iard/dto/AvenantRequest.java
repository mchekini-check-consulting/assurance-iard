package com.iard.dto;

import com.iard.entity.Formule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Modification des garanties d'un contrat actif en selfcare.
 * Seuls les champs non nuls sont appliqués ; les autres données du risque
 * (adresse, surface, occupation…) sont reprises du contrat d'origine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvenantRequest {

    private Formule formule;
    private List<String> optionsGaranties;

    // Sécurité & spécificités du bien
    private Boolean alarme;
    private Boolean porteBlindee;
    private Boolean dependances;
    private Integer surfaceDependances;
    private Boolean piscine;

    // Contenu
    private BigDecimal capitalMobilier;
    private Boolean objetsValeur;
    private BigDecimal valeurObjetsValeur;
}
