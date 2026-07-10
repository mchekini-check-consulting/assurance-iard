package com.iard.sinistre.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "dossiers_statuts_historique")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierStatutHistorique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    @ToString.Exclude
    private DossierSinistre dossier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDossier statut;

    @Column(name = "auteur")
    private String auteur;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_changement", nullable = false)
    @Builder.Default
    private LocalDateTime dateChangement = LocalDateTime.now();
}
