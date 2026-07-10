package com.iard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iard.dto.RibExtraction;
import com.iard.dto.TitreSejourExtraction;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * Implémentation du service d'extraction de documents via GPT-4 Vision.
 */
@Slf4j
@Service
public class GptVisionExtractionService implements DocumentExtractionService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${openai.model:gpt-4o}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GptVisionExtractionService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public TitreSejourExtraction extractTitreSejour(byte[] imageData, String contentType) {
        log.info("Extraction des données du titre de séjour via GPT Vision");

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Clé API OpenAI non configurée, utilisation du mock");
            return mockTitreSejourExtraction();
        }

        String prompt = """
            Analyse cette image d'un titre de séjour français et extrais les informations suivantes au format JSON:
            {
                "nom": "le nom de famille",
                "prenom": "le prénom",
                "numero": "le numéro du document",
                "dateExpiration": "la date d'expiration au format YYYY-MM-DD"
            }

            Si tu ne peux pas lire une information, mets null pour ce champ.
            Réponds UNIQUEMENT avec le JSON, sans texte supplémentaire.
            """;

        try {
            String response = callGptVision(imageData, contentType, prompt);
            return parseTitreSejourResponse(response);
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction du titre de séjour: {}", e.getMessage());
            return TitreSejourExtraction.builder()
                    .extractionReussie(false)
                    .erreur("Erreur lors de l'analyse du document: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public RibExtraction extractRib(byte[] imageData, String contentType) {
        log.info("Extraction des données du RIB via GPT Vision");

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Clé API OpenAI non configurée, utilisation du mock");
            return mockRibExtraction();
        }

        String prompt = """
            Analyse cette image d'un RIB (Relevé d'Identité Bancaire) français et extrais les informations suivantes au format JSON:
            {
                "nom": "le nom du titulaire",
                "prenom": "le prénom du titulaire",
                "banque": "le nom de la banque",
                "iban": "le numéro IBAN complet"
            }

            Si tu ne peux pas lire une information, mets null pour ce champ.
            Réponds UNIQUEMENT avec le JSON, sans texte supplémentaire.
            """;

        try {
            String response = callGptVision(imageData, contentType, prompt);
            return parseRibResponse(response);
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction du RIB: {}", e.getMessage());
            return RibExtraction.builder()
                    .extractionReussie(false)
                    .erreur("Erreur lors de l'analyse du document: " + e.getMessage())
                    .build();
        }
    }

    private String callGptVision(byte[] imageData, String contentType, String prompt) throws Exception {
        byte[] processedData = imageData;
        String mediaType = contentType != null ? contentType : "image/jpeg";

        // Si c'est un PDF, le convertir en image
        if (contentType != null && contentType.toLowerCase().contains("pdf")) {
            log.info("Conversion du PDF en image pour l'analyse GPT Vision");
            processedData = convertPdfToImage(imageData);
            mediaType = "image/png";
        }

        String base64Image = Base64.getEncoder().encodeToString(processedData);

        // Construire la requête
        Map<String, Object> imageContent = new LinkedHashMap<>();
        imageContent.put("type", "image_url");
        imageContent.put("image_url", Map.of(
                "url", "data:" + mediaType + ";base64," + base64Image,
                "detail", "high"
        ));

        Map<String, Object> textContent = new LinkedHashMap<>();
        textContent.put("type", "text");
        textContent.put("text", prompt);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", List.of(textContent, imageContent));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(message));
        requestBody.put("max_tokens", 500);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        // Parser la réponse
        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("choices").path(0).path("message").path("content").asText();
    }

    /**
     * Convertit la première page d'un PDF en image PNG.
     */
    private byte[] convertPdfToImage(byte[] pdfData) throws Exception {
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            PDFRenderer renderer = new PDFRenderer(document);
            // Rendre la première page à 200 DPI pour une bonne qualité OCR
            BufferedImage image = renderer.renderImageWithDPI(0, 200);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        }
    }

    private TitreSejourExtraction parseTitreSejourResponse(String response) {
        try {
            // Nettoyer la réponse (enlever les backticks markdown si présents)
            String cleanResponse = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();

            JsonNode json = objectMapper.readTree(cleanResponse);

            return TitreSejourExtraction.builder()
                    .nom(getTextOrNull(json, "nom"))
                    .prenom(getTextOrNull(json, "prenom"))
                    .numero(getTextOrNull(json, "numero"))
                    .dateExpiration(getTextOrNull(json, "dateExpiration"))
                    .extractionReussie(true)
                    .build();
        } catch (Exception e) {
            log.error("Erreur parsing réponse titre de séjour: {}", e.getMessage());
            return TitreSejourExtraction.builder()
                    .extractionReussie(false)
                    .erreur("Format de réponse invalide")
                    .build();
        }
    }

    private RibExtraction parseRibResponse(String response) {
        try {
            // Nettoyer la réponse
            String cleanResponse = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();

            JsonNode json = objectMapper.readTree(cleanResponse);

            return RibExtraction.builder()
                    .nom(getTextOrNull(json, "nom"))
                    .prenom(getTextOrNull(json, "prenom"))
                    .banque(getTextOrNull(json, "banque"))
                    .iban(getTextOrNull(json, "iban"))
                    .extractionReussie(true)
                    .build();
        } catch (Exception e) {
            log.error("Erreur parsing réponse RIB: {}", e.getMessage());
            return RibExtraction.builder()
                    .extractionReussie(false)
                    .erreur("Format de réponse invalide")
                    .build();
        }
    }

    private String getTextOrNull(JsonNode json, String field) {
        JsonNode node = json.path(field);
        if (node.isNull() || node.isMissingNode()) {
            return null;
        }
        String value = node.asText();
        return "null".equalsIgnoreCase(value) ? null : value;
    }

    // Mocks pour le développement sans clé API
    private TitreSejourExtraction mockTitreSejourExtraction() {
        return TitreSejourExtraction.builder()
                .nom("DUPONT")
                .prenom("Jean")
                .numero("TS1234567890")
                .dateExpiration("2027-12-31")
                .extractionReussie(true)
                .build();
    }

    private RibExtraction mockRibExtraction() {
        return RibExtraction.builder()
                .nom("DUPONT")
                .prenom("Jean")
                .banque("BNP PARIBAS")
                .iban("FR7630006000011234567890189")
                .extractionReussie(true)
                .build();
    }
}
