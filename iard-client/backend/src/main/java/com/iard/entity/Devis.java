package com.iard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "devis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Devis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Produit produit = Produit.HABITATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutDevis statut = StatutDevis.BROUILLON;

    @Column(name = "etape_courante")
    @Builder.Default
    private Integer etapeCourante = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "donnees_risque", columnDefinition = "jsonb")
    private DonneesRisqueHabitation donneesRisque;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "assure", columnDefinition = "jsonb")
    private PersonneAssuree assure;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resultat_tarif", columnDefinition = "jsonb")
    private ResultatTarification resultatTarif;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
