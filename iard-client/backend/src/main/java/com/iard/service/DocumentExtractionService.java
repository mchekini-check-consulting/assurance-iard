package com.iard.service;

import com.iard.dto.RibExtraction;
import com.iard.dto.TitreSejourExtraction;

/**
 * Interface pour l'extraction de données de documents via OCR.
 * Implémentation via GPT Vision ou autre provider.
 */
public interface DocumentExtractionService {

    /**
     * Extrait les informations d'un titre de séjour.
     *
     * @param imageData    Données de l'image (base64 ou bytes)
     * @param contentType  Type MIME du fichier
     * @return Les données extraites
     */
    TitreSejourExtraction extractTitreSejour(byte[] imageData, String contentType);

    /**
     * Extrait les informations d'un RIB.
     *
     * @param imageData    Données de l'image (base64 ou bytes)
     * @param contentType  Type MIME du fichier
     * @return Les données extraites
     */
    RibExtraction extractRib(byte[] imageData, String contentType);
}
