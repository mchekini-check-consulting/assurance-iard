package com.iard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "sinistre_statuts_historique")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SinistreStatutHistorique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id", nullable = false)
    @ToString.Exclude
    private Sinistre sinistre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutSinistre statut;

    @Column(name = "date_changement", nullable = false)
    @Builder.Default
    private LocalDateTime dateChangement = LocalDateTime.now();
}
