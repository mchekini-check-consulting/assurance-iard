package com.iard.service;

import com.iard.dto.FactureResponse;
import com.iard.entity.*;
import com.iard.repository.FactureRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service de gestion des factures.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FactureService {

    private final FactureRepository factureRepository;

    @Value("${app.pdf.storage-path:./pdfs}")
    private String storagePath;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter PERIODE_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private static final Color HEADER_BG = new Color(241, 245, 249);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);

    /**
     * Génère une facture après un paiement réussi.
     */
    @Transactional
    public Facture genererFacture(Paiement paiement, Contrat contrat, User user) {
        log.info("Génération de la facture pour le paiement {} du contrat {}",
                paiement.getId(), contrat.getNumeroContrat());

        // Calculer les montants (inverse de la TVA à 20%)
        BigDecimal montantTTC = paiement.getMontant();
        BigDecimal montantHT = montantTTC.divide(new BigDecimal("1.20"), 2, RoundingMode.HALF_UP);
        BigDecimal taxes = montantTTC.subtract(montantHT);

        String numeroFacture = genererNumeroFacture();

        Facture facture = Facture.builder()
                .numeroFacture(numeroFacture)
                .paiement(paiement)
                .contrat(contrat)
                .user(user)
                .montantHT(montantHT)
                .taxes(taxes)
                .montantTTC(montantTTC)
                .periode(paiement.getPeriode())
                .dateEmission(LocalDate.now())
                .build();

        facture = factureRepository.save(facture);

        // Générer le PDF
        try {
            String pdfPath = genererPdfFacture(facture, contrat, user, paiement);
            facture.setPdfPath(pdfPath);
            facture = factureRepository.save(facture);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF de la facture {}: {}",
                    numeroFacture, e.getMessage());
        }

        return facture;
    }

    /**
     * Récupère toutes les factures d'un utilisateur.
     */
    public List<FactureResponse> getFacturesUtilisateur(Long userId) {
        return factureRepository.findByUserIdOrderByDateEmissionDesc(userId)
                .stream()
                .map(this::toFactureResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une facture par son ID.
     */
    public FactureResponse getFacture(Long factureId, Long userId) {
        Facture facture = factureRepository.findByIdAndUserId(factureId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Facture non trouvée"));
        return toFactureResponse(facture);
    }

    /**
     * Récupère le contenu PDF d'une facture.
     */
    public byte[] getPdfContent(Long factureId, Long userId) throws IOException {
        Facture facture = factureRepository.findByIdAndUserId(factureId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Facture non trouvée"));

        if (facture.getPdfPath() == null) {
            throw new IllegalStateException("PDF non disponible pour cette facture");
        }

        Path filePath = Paths.get(storagePath).resolve(facture.getPdfPath());
        return Files.readAllBytes(filePath);
    }

    private String genererNumeroFacture() {
        String prefix = "FAC";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return prefix + "-" + timestamp + "-" + random;
    }

    private FactureResponse toFactureResponse(Facture facture) {
        Contrat contrat = facture.getContrat();
        Paiement paiement = facture.getPaiement();

        return FactureResponse.builder()
                .id(facture.getId())
                .numeroFacture(facture.getNumeroFacture())
                .contratId(contrat.getId())
                .numeroContrat(contrat.getNumeroContrat())
                .produit(contrat.getProduit().name())
                .montantHT(facture.getMontantHT())
                .taxes(facture.getTaxes())
                .montantTTC(facture.getMontantTTC())
                .periode(facture.getPeriode())
                .dateEmission(facture.getDateEmission())
                .datePaiement(paiement.getDatePrelevement())
                .pdfUrl("/api/factures/" + facture.getId() + "/pdf")
                .createdAt(facture.getCreatedAt())
                .build();
    }

    private String genererPdfFacture(Facture facture, Contrat contrat, User user, Paiement paiement)
            throws IOException, DocumentException {

        Path storageDir = Paths.get(storagePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        String fileName = "facture_" + facture.getNumeroFacture() + ".pdf";
        String filePath = storageDir.resolve(fileName).toString();

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Polices
        Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD, PRIMARY_COLOR);
        Font headerFont = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 10);
        Font smallFont = new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_MUTED);
        Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD);

        // En-tête
        Paragraph titre = new Paragraph("FACTURE", titleFont);
        titre.setAlignment(Element.ALIGN_CENTER);
        document.add(titre);

        Paragraph sousTitre = new Paragraph("N° " + facture.getNumeroFacture(), new Font(Font.HELVETICA, 12));
        sousTitre.setAlignment(Element.ALIGN_CENTER);
        sousTitre.setSpacingAfter(30);
        document.add(sousTitre);

        // Informations émetteur / client
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(30);

        // Émetteur
        PdfPCell emetteurCell = new PdfPCell();
        emetteurCell.setBorder(Rectangle.NO_BORDER);
        emetteurCell.addElement(new Paragraph("IARD Assurances SAS", boldFont));
        emetteurCell.addElement(new Paragraph("123 Avenue des Assurances", normalFont));
        emetteurCell.addElement(new Paragraph("75008 Paris", normalFont));
        emetteurCell.addElement(new Paragraph("RCS Paris 123 456 789", smallFont));
        emetteurCell.addElement(new Paragraph("TVA FR12345678901", smallFont));
        infoTable.addCell(emetteurCell);

        // Client
        PdfPCell clientCell = new PdfPCell();
        clientCell.setBorder(Rectangle.NO_BORDER);
        clientCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph clientPara = new Paragraph();
        clientPara.add(new Chunk("FACTURÉ À\n", new Font(Font.HELVETICA, 8, Font.BOLD, TEXT_MUTED)));
        String civilite = user.getCivilite() == Civilite.MONSIEUR ? "M." : "Mme";
        clientPara.add(new Chunk(civilite + " " + user.getPrenom() + " " + user.getNom() + "\n", boldFont));
        clientPara.add(new Chunk(user.getEmail(), normalFont));
        clientPara.setAlignment(Element.ALIGN_RIGHT);
        clientCell.addElement(clientPara);
        infoTable.addCell(clientCell);

        document.add(infoTable);

        // Informations facture
        PdfPTable detailsTable = new PdfPTable(2);
        detailsTable.setWidthPercentage(100);
        detailsTable.setSpacingAfter(20);

        addDetailRow(detailsTable, "Date d'émission", facture.getDateEmission().format(DATE_FORMATTER), normalFont);
        addDetailRow(detailsTable, "Date de paiement", paiement.getDatePrelevement().format(DATE_FORMATTER), normalFont);
        addDetailRow(detailsTable, "Contrat N°", contrat.getNumeroContrat(), normalFont);
        addDetailRow(detailsTable, "Produit", "Assurance " + contrat.getProduit().name(), normalFont);

        LocalDate periodeDate = LocalDate.parse(facture.getPeriode() + "-01");
        addDetailRow(detailsTable, "Période couverte", periodeDate.format(PERIODE_DISPLAY_FORMATTER), normalFont);

        document.add(detailsTable);

        // Ligne de séparation
        document.add(new Paragraph(" "));

        // Tableau des montants
        PdfPTable montantsTable = new PdfPTable(2);
        montantsTable.setWidthPercentage(60);
        montantsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        montantsTable.setSpacingBefore(20);

        addMontantRow(montantsTable, "Montant HT", formatMontant(facture.getMontantHT()), normalFont);
        addMontantRow(montantsTable, "TVA (20%)", formatMontant(facture.getTaxes()), normalFont);

        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL TTC", boldFont));
        totalLabelCell.setBorder(Rectangle.TOP);
        totalLabelCell.setBorderColorTop(new Color(226, 232, 240));
        totalLabelCell.setPaddingTop(10);
        montantsTable.addCell(totalLabelCell);

        PdfPCell totalValueCell = new PdfPCell(new Phrase(formatMontant(facture.getMontantTTC()),
                new Font(Font.HELVETICA, 12, Font.BOLD, PRIMARY_COLOR)));
        totalValueCell.setBorder(Rectangle.TOP);
        totalValueCell.setBorderColorTop(new Color(226, 232, 240));
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setPaddingTop(10);
        montantsTable.addCell(totalValueCell);

        document.add(montantsTable);

        // Mention de paiement
        Paragraph paiementMention = new Paragraph();
        paiementMention.setSpacingBefore(40);
        paiementMention.add(new Chunk("✓ Payé le " + paiement.getDatePrelevement().format(DATE_FORMATTER),
                new Font(Font.HELVETICA, 11, Font.BOLD, new Color(16, 185, 129))));
        paiementMention.setAlignment(Element.ALIGN_CENTER);
        document.add(paiementMention);

        // Pied de page
        Paragraph footer = new Paragraph();
        footer.setSpacingBefore(60);
        footer.add(new Chunk("IARD Assurances SAS - Capital social 1 000 000 € - RCS Paris 123 456 789\n", smallFont));
        footer.add(new Chunk("123 Avenue des Assurances, 75008 Paris - contact@iard-assurances.fr", smallFont));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();

        log.info("PDF facture généré: {}", filePath);
        return fileName;
    }

    private void addDetailRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_MUTED)));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    private void addMontantRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(6);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(6);
        table.addCell(valueCell);
    }

    private String formatMontant(BigDecimal montant) {
        if (montant == null) return "-";
        return String.format("%,.2f €", montant).replace(",", " ");
    }
}
