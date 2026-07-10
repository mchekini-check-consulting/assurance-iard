package com.iard.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Événement publié sur le topic sinistres.declares après persistance d'une déclaration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SinistreDeclareEvent {

    @Builder.Default
    private Integer eventVersion = 1;

    private Long sinistreId;
    private String numeroSinistre;
    private Long contratId;
    private String numeroContrat;
    private Long userId;
    private String souscripteurNom;
    private String souscripteurPrenom;
    private String type;
    private LocalDate dateSinistre;
    private String lieu;
    private String description;
    private BigDecimal montantEstime;
    private LocalDateTime dateDeclaration;
}
