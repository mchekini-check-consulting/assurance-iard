package com.iard.sinistre.entity;

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
@Table(name = "dossiers_sinistres")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierSinistre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identifiant du sinistre côté plateforme de souscription — unique : garantit l'idempotence du consumer
    @Column(name = "sinistre_id", nullable = false, unique = true)
    private Long sinistreId;

    @Column(name = "numero_sinistre", nullable = false)
    private String numeroSinistre;

    @Column(name = "contrat_id", nullable = false)
    private Long contratId;

    @Column(name = "numero_contrat")
    private String numeroContrat;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "souscripteur_nom")
    private String souscripteurNom;

    @Column(name = "souscripteur_prenom")
    private String souscripteurPrenom;

    @Column(nullable = false)
    private String type;

    @Column(name = "date_sinistre", nullable = false)
    private LocalDate dateSinistre;

    private String lieu;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "montant_estime", precision = 12, scale = 2)
    private BigDecimal montantEstime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutDossier statut = StatutDossier.A_TRAITER;

    @Column(name = "montant_rembourse", precision = 12, scale = 2)
    private BigDecimal montantRembourse;

    @Column(name = "commentaire_decision", columnDefinition = "TEXT")
    private String commentaireDecision;

    @Column(name = "decide_par")
    private String decidePar;

    @Column(name = "date_decision")
    private LocalDateTime dateDecision;

    @Column(name = "date_declaration")
    private LocalDateTime dateDeclaration;

    // Rattrapage : true tant que la dernière décision n'a pas été publiée sur Kafka
    @Column(name = "sync_en_attente", nullable = false)
    @Builder.Default
    private boolean syncEnAttente = false;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateChangement ASC")
    @Builder.Default
    private List<DossierStatutHistorique> historiqueStatuts = new ArrayList<>();

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
