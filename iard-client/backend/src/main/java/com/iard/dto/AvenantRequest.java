package com.iard.dto;

import com.iard.entity.Formule;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Garanties à modifier sur un contrat actif. Seuls les champs renseignés sont appliqués ; "
        + "les autres données du risque sont reprises du contrat d'origine.")
public class AvenantRequest {

    @Schema(description = "Nouvelle formule du contrat", example = "PREMIUM")
    private Formule formule;
    @Schema(description = "Options de garanties souscrites (remplace la liste actuelle) : BRIS_GLACE, "
            + "VOL_HORS_DOMICILE, JARDIN, PISCINE_PLUS, DOMMAGES_ELECTRIQUES, ASSISTANCE_PLUS",
            example = "[\"BRIS_GLACE\", \"ASSISTANCE_PLUS\"]")
    private List<String> optionsGaranties;

    // Sécurité & spécificités du bien
    private Boolean alarme;
    private Boolean porteBlindee;
    private Boolean dependances;
    private Integer surfaceDependances;
    private Boolean piscine;

    // Contenu
    @Schema(description = "Capital mobilier assuré en euros", example = "25000")
    private BigDecimal capitalMobilier;
    private Boolean objetsValeur;
    private BigDecimal valeurObjetsValeur;
}
