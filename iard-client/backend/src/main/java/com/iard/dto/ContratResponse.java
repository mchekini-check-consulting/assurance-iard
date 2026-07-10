package com.iard.dto;

import com.iard.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratResponse {

    private Long id;
    private String numeroContrat;
    private Long devisId;
    private Produit produit;
    private Formule formule;
    private ResultatTarification garanties;
    private BigDecimal primeHT;
    private BigDecimal taxes;
    private BigDecimal primeTTC;
    private Periodicite periodicite;
    private StatutContrat statut;
    private LocalDateTime dateSignature;
    private String signatureId;
    private String pdfUrl;
    private LocalDateTime createdAt;

    // Infos prélèvement
    private LocalDate prochaineDatePrelevement;
    private BigDecimal montantMensuelTTC;

    // Infos du bien assuré
    private DonneesRisqueHabitation donneesRisque;
    private PersonneAssuree assure;

    // Infos du souscripteur
    private String souscripteurNom;
    private String souscripteurPrenom;
    private String souscripteurEmail;
}
