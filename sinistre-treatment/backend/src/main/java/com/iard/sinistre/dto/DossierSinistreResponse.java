package com.iard.sinistre.dto;

import com.iard.sinistre.entity.DossierSinistre;
import com.iard.sinistre.entity.StatutDossier;
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
public class DossierSinistreResponse {

    private Long id;
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
    private StatutDossier statut;
    private BigDecimal montantRembourse;
    private String commentaireDecision;
    private String decidePar;
    private LocalDateTime dateDecision;
    private LocalDateTime dateDeclaration;
    private LocalDateTime dateReception;
    private List<HistoriqueDto> historique;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoriqueDto {
        private StatutDossier statut;
        private String auteur;
        private String commentaire;
        private LocalDateTime date;
    }

    public static DossierSinistreResponse fromEntity(DossierSinistre dossier) {
        return DossierSinistreResponse.builder()
                .id(dossier.getId())
                .sinistreId(dossier.getSinistreId())
                .numeroSinistre(dossier.getNumeroSinistre())
                .contratId(dossier.getContratId())
                .numeroContrat(dossier.getNumeroContrat())
                .userId(dossier.getUserId())
                .souscripteurNom(dossier.getSouscripteurNom())
                .souscripteurPrenom(dossier.getSouscripteurPrenom())
                .type(dossier.getType())
                .dateSinistre(dossier.getDateSinistre())
                .lieu(dossier.getLieu())
                .description(dossier.getDescription())
                .montantEstime(dossier.getMontantEstime())
                .statut(dossier.getStatut())
                .montantRembourse(dossier.getMontantRembourse())
                .commentaireDecision(dossier.getCommentaireDecision())
                .decidePar(dossier.getDecidePar())
                .dateDecision(dossier.getDateDecision())
                .dateDeclaration(dossier.getDateDeclaration())
                .dateReception(dossier.getCreatedAt())
                .historique(dossier.getHistoriqueStatuts().stream()
                        .map(h -> HistoriqueDto.builder()
                                .statut(h.getStatut())
                                .auteur(h.getAuteur())
                                .commentaire(h.getCommentaire())
                                .date(h.getDateChangement())
                                .build())
                        .toList())
                .build();
    }
}
