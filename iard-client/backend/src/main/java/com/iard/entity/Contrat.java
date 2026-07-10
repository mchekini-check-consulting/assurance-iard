package com.iard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contrats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_contrat", nullable = false, unique = true)
    private String numeroContrat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devis_id", nullable = false)
    private Devis devis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "assure", columnDefinition = "jsonb")
    private PersonneAssuree assure;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Produit produit = Produit.HABITATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Formule formule;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "garanties", columnDefinition = "jsonb")
    private ResultatTarification garanties;

    @Column(name = "prime_ht", nullable = false, precision = 10, scale = 2)
    private BigDecimal primeHT;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxes;

    @Column(name = "prime_ttc", nullable = false, precision = 10, scale = 2)
    private BigDecimal primeTTC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Periodicite periodicite = Periodicite.ANNUELLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutContrat statut = StatutContrat.EN_ATTENTE;

    @Column(name = "date_signature")
    private LocalDateTime dateSignature;

    @Column(name = "signature_id")
    private String signatureId;

    @Column(name = "pdf_path")
    private String pdfPath;

    @Column(name = "prochaine_date_prelevement")
    private LocalDate prochaineDatePrelevement;

    @Column(name = "montant_mensuel_ttc", precision = 10, scale = 2)
    private BigDecimal montantMensuelTTC;

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
