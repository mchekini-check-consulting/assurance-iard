package com.iard.dto;

import com.iard.entity.Sinistre;
import com.iard.entity.StatutSinistre;
import com.iard.entity.TypeSinistre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SinistreResponse {

    private Long id;
    private String numeroSinistre;
    private Long contratId;
    private String numeroContrat;
    private TypeSinistre type;
    private LocalDate dateSinistre;
    private String lieu;
    private String description;
    private BigDecimal montantEstime;
    private StatutSinistre statut;
    private BigDecimal montantRembourse;
    private String commentaireDecision;
    private LocalDateTime dateDecision;
    private LocalDateTime createdAt;
    private List<PieceJointeDto> piecesJointes;
    private List<EtapeStatutDto> timeline;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieceJointeDto {
        private Long id;
        private String nomFichier;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EtapeStatutDto {
        private StatutSinistre statut;
        private LocalDateTime date;
    }

    public static SinistreResponse fromEntity(Sinistre sinistre) {
        return SinistreResponse.builder()
                .id(sinistre.getId())
                .numeroSinistre(sinistre.getNumeroSinistre())
                .contratId(sinistre.getContrat().getId())
                .numeroContrat(sinistre.getContrat().getNumeroContrat())
                .type(sinistre.getType())
                .dateSinistre(sinistre.getDateSinistre())
                .lieu(sinistre.getLieu())
                .description(sinistre.getDescription())
                .montantEstime(sinistre.getMontantEstime())
                .statut(sinistre.getStatut())
                .montantRembourse(sinistre.getMontantRembourse())
                .commentaireDecision(sinistre.getCommentaireDecision())
                .dateDecision(sinistre.getDateDecision())
                .createdAt(sinistre.getCreatedAt())
                .piecesJointes(sinistre.getPiecesJointes().stream()
                        .map(pj -> PieceJointeDto.builder()
                                .id(pj.getId())
                                .nomFichier(pj.getNomFichier())
                                .build())
                        .toList())
                .timeline(sinistre.getHistoriqueStatuts().stream()
                        .map(h -> EtapeStatutDto.builder()
                                .statut(h.getStatut())
                                .date(h.getDateChangement())
                                .build())
                        .toList())
                .build();
    }
}
