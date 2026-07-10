package com.iard.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utilitaire de comparaison de noms.
 * Comparaison normalisée : insensible à la casse et aux accents,
 * trim des espaces, tolérance à l'inversion nom/prénom.
 */
public final class NameMatcher {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private NameMatcher() {
        // Utility class
    }

    /**
     * Compare nom et prénom extraits avec ceux du profil.
     * Tolère l'inversion nom/prénom.
     *
     * @param extractedNom    Nom extrait du document
     * @param extractedPrenom Prénom extrait du document
     * @param profileNom      Nom du profil utilisateur
     * @param profilePrenom   Prénom du profil utilisateur
     * @return true si les noms correspondent
     */
    public static boolean matches(String extractedNom, String extractedPrenom,
                                   String profileNom, String profilePrenom) {
        String normExtractedNom = normalize(extractedNom);
        String normExtractedPrenom = normalize(extractedPrenom);
        String normProfileNom = normalize(profileNom);
        String normProfilePrenom = normalize(profilePrenom);

        // Comparaison directe
        boolean directMatch = normExtractedNom.equals(normProfileNom) &&
                              normExtractedPrenom.equals(normProfilePrenom);

        // Comparaison avec inversion nom/prénom
        boolean invertedMatch = normExtractedNom.equals(normProfilePrenom) &&
                                normExtractedPrenom.equals(normProfileNom);

        return directMatch || invertedMatch;
    }

    /**
     * Compare un seul nom (pour cas où nom et prénom sont dans un seul champ).
     */
    public static boolean matchesSingleField(String extractedFullName,
                                              String profileNom,
                                              String profilePrenom) {
        String normExtracted = normalize(extractedFullName);
        String normProfileNom = normalize(profileNom);
        String normProfilePrenom = normalize(profilePrenom);

        // Le nom complet extrait doit contenir les deux parties
        String fullName1 = normProfilePrenom + " " + normProfileNom;
        String fullName2 = normProfileNom + " " + normProfilePrenom;

        return normExtracted.equals(fullName1) ||
               normExtracted.equals(fullName2) ||
               normExtracted.contains(normProfileNom) && normExtracted.contains(normProfilePrenom);
    }

    /**
     * Normalise une chaîne : trim, lowercase, suppression des accents,
     * normalisation des espaces multiples.
     */
    public static String normalize(String input) {
        if (input == null) {
            return "";
        }

        // Trim et lowercase
        String result = input.trim().toLowerCase();

        // Supprimer les accents
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = DIACRITICS_PATTERN.matcher(result).replaceAll("");

        // Normaliser les espaces multiples
        result = result.replaceAll("\\s+", " ");

        // Supprimer les caractères spéciaux sauf espaces
        result = result.replaceAll("[^a-z0-9 ]", "");

        return result;
    }
}
