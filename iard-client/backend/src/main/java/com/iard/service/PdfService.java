package com.iard.service;

import com.iard.entity.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class PdfService {

    @Value("${app.pdf.storage-path:./pdfs}")
    private String storagePath;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private static final Color HEADER_BG = new Color(241, 245, 249);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);

    public String genererContratPdf(Contrat contrat, User souscripteur, Devis devis) throws IOException, DocumentException {
        // Créer le répertoire de stockage si nécessaire
        Path storageDir = Paths.get(storagePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        String fileName = "contrat_" + contrat.getNumeroContrat() + ".pdf";
        String filePath = storageDir.resolve(fileName).toString();

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Polices
        Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, PRIMARY_COLOR);
        Font headerFont = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 10);
        Font smallFont = new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_MUTED);
        Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD);

        // En-tête
        ajouterEntete(document, contrat, souscripteur, devis, titleFont, normalFont, smallFont);

        // Objet du contrat
        ajouterObjetContrat(document, devis, headerFont, normalFont);

        // Formule souscrite
        ajouterFormule(document, contrat, headerFont, normalFont, boldFont);

        // Tableau des garanties
        ajouterGaranties(document, contrat, headerFont, normalFont, boldFont);

        // Tarification
        ajouterTarification(document, contrat, headerFont, normalFont, boldFont);

        // CGV
        ajouterCGV(document, headerFont, smallFont);

        // Règles de résiliation
        ajouterResiliation(document, headerFont, smallFont);

        // Zone de signature
        ajouterZoneSignature(document, contrat, souscripteur, headerFont, normalFont, boldFont);

        document.close();

        log.info("PDF généré: {}", filePath);
        return fileName;
    }

    public String regenererPdfSigne(Contrat contrat, User souscripteur, Devis devis) throws IOException, DocumentException {
        return genererContratPdf(contrat, souscripteur, devis);
    }

    private void ajouterEntete(Document document, Contrat contrat, User souscripteur, Devis devis,
                                Font titleFont, Font normalFont, Font smallFont) throws DocumentException {
        // Logo et titre
        Paragraph titre = new Paragraph("IARD Assurances", titleFont);
        titre.setAlignment(Element.ALIGN_CENTER);
        document.add(titre);

        Paragraph sousTitre = new Paragraph("Contrat d'Assurance Habitation", new Font(Font.HELVETICA, 14));
        sousTitre.setAlignment(Element.ALIGN_CENTER);
        sousTitre.setSpacingAfter(20);
        document.add(sousTitre);

        // Infos contrat
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(20);

        // Colonne gauche - Infos contrat
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Paragraph("N° de contrat : " + contrat.getNumeroContrat(), normalFont));
        leftCell.addElement(new Paragraph("Date d'édition : " + LocalDateTime.now().format(DATE_FORMATTER), normalFont));
        leftCell.addElement(new Paragraph("Statut : " + (contrat.getDateSignature() != null ? "Signé" : "En attente de signature"), normalFont));
        infoTable.addCell(leftCell);

        // Colonne droite - Assureur
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph assureur = new Paragraph();
        assureur.add(new Chunk("IARD Assurances SAS\n", new Font(Font.HELVETICA, 10, Font.BOLD)));
        assureur.add(new Chunk("123 Avenue des Assurances\n75008 Paris\nRCS Paris 123 456 789", smallFont));
        assureur.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(assureur);
        infoTable.addCell(rightCell);

        document.add(infoTable);

        // Séparateur
        document.add(new Paragraph(" "));

        // Souscripteur
        PdfPTable partiesTable = new PdfPTable(2);
        partiesTable.setWidthPercentage(100);
        partiesTable.setSpacingAfter(20);

        PdfPCell souscripteurCell = createInfoCell("SOUSCRIPTEUR",
                (souscripteur.getCivilite() == Civilite.MONSIEUR ? "M. " : "Mme ") +
                        souscripteur.getPrenom() + " " + souscripteur.getNom() + "\n" +
                        souscripteur.getEmail(),
                normalFont, smallFont);
        partiesTable.addCell(souscripteurCell);

        // Assuré (si différent)
        PersonneAssuree assure = contrat.getAssure() != null ? contrat.getAssure() : null;
        String assureInfo;
        if (assure != null) {
            assureInfo = (assure.getCivilite() == Civilite.MONSIEUR ? "M. " : "Mme ") +
                    assure.getPrenom() + " " + assure.getNom() + "\n" +
                    assure.getEmail();
        } else {
            assureInfo = "Identique au souscripteur";
        }
        PdfPCell assureCell = createInfoCell("ASSURÉ", assureInfo, normalFont, smallFont);
        partiesTable.addCell(assureCell);

        document.add(partiesTable);
    }

    private PdfPCell createInfoCell(String title, String content, Font normalFont, Font smallFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setPadding(10);
        cell.setBackgroundColor(HEADER_BG);

        Paragraph p = new Paragraph();
        p.add(new Chunk(title + "\n", new Font(Font.HELVETICA, 8, Font.BOLD, TEXT_MUTED)));
        p.add(new Chunk(content, normalFont));
        cell.addElement(p);

        return cell;
    }

    private void ajouterObjetContrat(Document document, Devis devis, Font headerFont, Font normalFont) throws DocumentException {
        Paragraph titre = new Paragraph("Objet du contrat", headerFont);
        titre.setSpacingBefore(10);
        titre.setSpacingAfter(10);
        document.add(titre);

        DonneesRisqueHabitation donnees = devis.getDonneesRisque();
        String typeBien = donnees.getTypeBien() == TypeBien.APPARTEMENT ? "Appartement" : "Maison";
        String typeResidence = donnees.getTypeResidence() == TypeResidence.PRINCIPALE ? "Résidence principale" : "Résidence secondaire";

        Paragraph objet = new Paragraph();
        objet.add(new Chunk("Assurance Multirisque Habitation pour : " + typeBien + " (" + typeResidence + ")\n", normalFont));
        objet.add(new Chunk("Adresse : " + donnees.getAdresse() + ", " + donnees.getCodePostal() + " " + donnees.getVille() + "\n", normalFont));
        objet.add(new Chunk("Surface : " + donnees.getSurfaceHabitable() + " m² - " + donnees.getNombrePieces() + " pièces", normalFont));
        objet.setSpacingAfter(15);
        document.add(objet);
    }

    private void ajouterFormule(Document document, Contrat contrat, Font headerFont, Font normalFont, Font boldFont) throws DocumentException {
        Paragraph titre = new Paragraph("Formule souscrite", headerFont);
        titre.setSpacingBefore(10);
        titre.setSpacingAfter(10);
        document.add(titre);

        String formuleLabel = switch (contrat.getFormule()) {
            case ESSENTIELLE -> "Essentielle - Protection de base";
            case CONFORT -> "Confort - Protection étendue";
            case PREMIUM -> "Premium - Protection maximale";
        };

        Paragraph formule = new Paragraph(formuleLabel, boldFont);
        formule.setSpacingAfter(15);
        document.add(formule);
    }

    private void ajouterGaranties(Document document, Contrat contrat, Font headerFont, Font normalFont, Font boldFont) throws DocumentException {
        Paragraph titre = new Paragraph("Tableau des garanties", headerFont);
        titre.setSpacingBefore(10);
        titre.setSpacingAfter(10);
        document.add(titre);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1.5f, 1.5f, 1.5f});

        // En-têtes
        addTableHeader(table, "Garantie", boldFont);
        addTableHeader(table, "Type", boldFont);
        addTableHeader(table, "Plafond", boldFont);
        addTableHeader(table, "Franchise", boldFont);

        ResultatTarification garanties = contrat.getGaranties();
        if (garanties != null) {
            // Garanties incluses
            if (garanties.getGarantiesIncluses() != null) {
                for (ResultatTarification.GarantieDetail g : garanties.getGarantiesIncluses()) {
                    addTableCell(table, g.getLibelle(), normalFont);
                    addTableCell(table, "Incluse", normalFont);
                    addTableCell(table, g.getPlafond() != null ? formatMontant(g.getPlafond()) : "-", normalFont);
                    addTableCell(table, g.getFranchise() != null && g.getFranchise().compareTo(BigDecimal.ZERO) > 0 ?
                            formatMontant(g.getFranchise()) : "-", normalFont);
                }
            }
            // Garanties optionnelles
            if (garanties.getGarantiesOptionnelles() != null) {
                for (ResultatTarification.GarantieDetail g : garanties.getGarantiesOptionnelles()) {
                    addTableCell(table, g.getLibelle(), normalFont);
                    addTableCell(table, "Option", normalFont);
                    addTableCell(table, "-", normalFont);
                    addTableCell(table, "+" + formatMontant(g.getPrimeSupplementaire()) + "/an", normalFont);
                }
            }
        }

        table.setSpacingAfter(15);
        document.add(table);
    }

    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(8);
        cell.setBorderColor(new Color(226, 232, 240));
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBorderColor(new Color(226, 232, 240));
        table.addCell(cell);
    }

    private void ajouterTarification(Document document, Contrat contrat, Font headerFont, Font normalFont, Font boldFont) throws DocumentException {
        Paragraph titre = new Paragraph("Tarification", headerFont);
        titre.setSpacingBefore(10);
        titre.setSpacingAfter(10);
        document.add(titre);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        addTarifLine(table, "Prime HT", formatMontant(contrat.getPrimeHT()), normalFont);
        addTarifLine(table, "Taxes et contributions", formatMontant(contrat.getTaxes()), normalFont);
        addTarifLine(table, "Prime TTC annuelle", formatMontant(contrat.getPrimeTTC()), boldFont);

        BigDecimal mensuel = contrat.getPrimeTTC().divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);
        addTarifLine(table, "Soit par mois", formatMontant(mensuel), normalFont);

        table.setSpacingAfter(15);
        document.add(table);
    }

    private void addTarifLine(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void ajouterCGV(Document document, Font headerFont, Font smallFont) throws DocumentException {
        Paragraph titre = new Paragraph("Conditions Générales", headerFont);
        titre.setSpacingBefore(15);
        titre.setSpacingAfter(10);
        document.add(titre);

        String cgv = """
                Durée du contrat : Le présent contrat est conclu pour une durée d'un an à compter de sa date d'effet. \
                Il se renouvelle par tacite reconduction pour des périodes successives d'un an, sauf résiliation par l'une des parties.

                Prise d'effet : Le contrat prend effet à la date indiquée aux conditions particulières, sous réserve du paiement \
                de la première prime.

                Obligations de l'assuré : L'assuré s'engage à déclarer exactement les circonstances du risque, à payer les primes \
                aux échéances convenues, à déclarer tout sinistre dans les délais prévus et à prendre toutes mesures pour limiter \
                les conséquences du sinistre.

                Exclusions : Sont exclus les dommages intentionnellement causés par l'assuré, les dommages résultant d'une guerre \
                civile ou étrangère, les dommages nucléaires, ainsi que les amendes et pénalités.

                Déclaration de sinistre : Tout sinistre doit être déclaré dans les 5 jours ouvrés suivant sa survenance \
                (2 jours en cas de vol). La déclaration peut être effectuée en ligne ou par courrier recommandé.
                """;

        Paragraph cgvPara = new Paragraph(cgv, smallFont);
        cgvPara.setSpacingAfter(10);
        document.add(cgvPara);
    }

    private void ajouterResiliation(Document document, Font headerFont, Font smallFont) throws DocumentException {
        Paragraph titre = new Paragraph("Modalités de résiliation", headerFont);
        titre.setSpacingBefore(10);
        titre.setSpacingAfter(10);
        document.add(titre);

        String resiliation = """
                Résiliation à échéance : Chaque partie peut résilier le contrat à l'échéance annuelle moyennant un préavis \
                de 2 mois avant la date d'échéance.

                Droit de renonciation : Conformément à l'article L112-2-1 du Code des assurances, vous disposez d'un délai de \
                14 jours calendaires à compter de la signature pour renoncer au contrat sans pénalité.

                Résiliation après la première année (Loi Hamon) : Après la première année de souscription, vous pouvez résilier \
                à tout moment, sans frais ni pénalités. La résiliation prend effet un mois après réception de votre demande.

                Résiliation pour changement de situation : En cas de déménagement, vente du bien assuré, ou changement de situation \
                matrimoniale, vous pouvez résilier le contrat dans les 3 mois suivant l'événement.
                """;

        Paragraph resiliationPara = new Paragraph(resiliation, smallFont);
        resiliationPara.setSpacingAfter(15);
        document.add(resiliationPara);
    }

    private void ajouterZoneSignature(Document document, Contrat contrat, User souscripteur,
                                       Font headerFont, Font normalFont, Font boldFont) throws DocumentException {
        Paragraph titre = new Paragraph("Signature", headerFont);
        titre.setSpacingBefore(20);
        titre.setSpacingAfter(10);
        document.add(titre);

        PdfPTable signatureTable = new PdfPTable(1);
        signatureTable.setWidthPercentage(60);
        signatureTable.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setPadding(15);
        cell.setMinimumHeight(80);

        Paragraph signatureContent = new Paragraph();
        signatureContent.add(new Chunk("Lu et approuvé\n\n", normalFont));

        if (contrat.getDateSignature() != null) {
            signatureContent.add(new Chunk("✓ Signé électroniquement le " +
                    contrat.getDateSignature().format(DATETIME_FORMATTER) + "\n", boldFont));
            signatureContent.add(new Chunk("par " + souscripteur.getPrenom() + " " + souscripteur.getNom() + "\n", normalFont));
            signatureContent.add(new Chunk("Réf. signature : " + contrat.getSignatureId(), new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_MUTED)));
        } else {
            signatureContent.add(new Chunk("En attente de signature électronique", new Font(Font.HELVETICA, 10, Font.ITALIC, TEXT_MUTED)));
        }

        cell.addElement(signatureContent);
        signatureTable.addCell(cell);

        document.add(signatureTable);
    }

    private String formatMontant(BigDecimal montant) {
        if (montant == null) return "-";
        return String.format("%,.2f €", montant).replace(",", " ");
    }

    public byte[] getPdfContent(String fileName) throws IOException {
        Path filePath = Paths.get(storagePath).resolve(fileName);
        return Files.readAllBytes(filePath);
    }
}
