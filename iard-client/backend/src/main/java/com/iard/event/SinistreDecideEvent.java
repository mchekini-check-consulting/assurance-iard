package com.iard.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Événement reçu du topic sinistres.decisions, publié par sinistre-treatment
 * à chaque décision du gestionnaire.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SinistreDecideEvent {

    @Builder.Default
    private Integer eventVersion = 1;

    private Long sinistreId;
    private String numeroSinistre;
    private String statut;
    private BigDecimal montantRembourse;
    private String commentaireDecision;
    private String decidePar;
    private LocalDateTime dateDecision;
}
