package com.iard.util;

import java.math.BigInteger;

/**
 * Utilitaire de validation d'IBAN.
 * Supporte la validation du format français et le checksum mod-97 (ISO 13616).
 */
public final class IbanValidator {

    private static final int IBAN_FR_LENGTH = 27;
    private static final BigInteger MOD_97 = BigInteger.valueOf(97);

    private IbanValidator() {
        // Utility class
    }

    /**
     * Valide un IBAN français.
     *
     * @param iban L'IBAN à valider
     * @return true si l'IBAN est valide
     */
    public static boolean isValidFrenchIban(String iban) {
        if (iban == null) {
            return false;
        }

        // Nettoyer l'IBAN (supprimer espaces et tirets, mettre en majuscules)
        String cleanIban = iban.replaceAll("[\\s-]", "").toUpperCase();

        // Vérifier le format français
        if (!isValidFrenchFormat(cleanIban)) {
            return false;
        }

        // Vérifier le checksum mod-97
        return isValidChecksum(cleanIban);
    }

    /**
     * Vérifie le format d'un IBAN français.
     * Format: FR + 2 chiffres de contrôle + 23 caractères alphanumériques
     */
    private static boolean isValidFrenchFormat(String iban) {
        if (iban.length() != IBAN_FR_LENGTH) {
            return false;
        }

        if (!iban.startsWith("FR")) {
            return false;
        }

        // Les 2 caractères suivants doivent être des chiffres (clé de contrôle)
        String checkDigits = iban.substring(2, 4);
        if (!checkDigits.matches("\\d{2}")) {
            return false;
        }

        // Le reste doit être alphanumérique
        String bban = iban.substring(4);
        return bban.matches("[A-Z0-9]+");
    }

    /**
     * Vérifie le checksum mod-97 selon ISO 13616.
     * 1. Déplacer les 4 premiers caractères à la fin
     * 2. Convertir les lettres en chiffres (A=10, B=11, ..., Z=35)
     * 3. Calculer modulo 97 - le résultat doit être 1
     */
    private static boolean isValidChecksum(String iban) {
        // Déplacer les 4 premiers caractères à la fin
        String rearranged = iban.substring(4) + iban.substring(0, 4);

        // Convertir les lettres en chiffres
        StringBuilder numericIban = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                // A=10, B=11, ..., Z=35
                numericIban.append(c - 'A' + 10);
            } else {
                numericIban.append(c);
            }
        }

        // Calculer modulo 97
        BigInteger ibanNumber = new BigInteger(numericIban.toString());
        return ibanNumber.mod(MOD_97).intValue() == 1;
    }

    /**
     * Formate un IBAN avec des espaces tous les 4 caractères.
     */
    public static String formatIban(String iban) {
        if (iban == null) {
            return null;
        }

        String cleanIban = iban.replaceAll("[\\s-]", "").toUpperCase();
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < cleanIban.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(' ');
            }
            formatted.append(cleanIban.charAt(i));
        }

        return formatted.toString();
    }
}
