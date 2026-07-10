package com.iard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sinistres")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sinistre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_sinistre", nullable = false, unique = true)
    private String numeroSinistre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id", nullable = false)
    private Contrat contrat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeSinistre type;

    @Column(name = "date_sinistre", nullable = false)
    private LocalDate dateSinistre;

    @Column(nullable = false)
    private String lieu;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "montant_estime", precision = 12, scale = 2)
    private BigDecimal montantEstime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutSinistre statut = StatutSinistre.DECLARE;

    @Column(name = "montant_rembourse", precision = 12, scale = 2)
    private BigDecimal montantRembourse;

    @Column(name = "commentaire_decision", columnDefinition = "TEXT")
    private String commentaireDecision;

    @Column(name = "date_decision")
    private LocalDateTime dateDecision;

    // Rattrapage : false tant que l'événement SinistreDeclare n'a pas été publié sur Kafka
    @Column(name = "event_publie", nullable = false)
    @Builder.Default
    private boolean eventPublie = false;

    @OneToMany(mappedBy = "sinistre", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SinistrePieceJointe> piecesJointes = new ArrayList<>();

    @OneToMany(mappedBy = "sinistre", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateChangement ASC")
    @Builder.Default
    private List<SinistreStatutHistorique> historiqueStatuts = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
