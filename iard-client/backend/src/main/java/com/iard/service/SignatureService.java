package com.iard.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service de signature électronique mocké.
 * En production, ce service sera remplacé par un vrai prestataire eIDAS.
 */
@Slf4j
@Service
public class SignatureService {

    private static final String MOCK_OTP_CODE = "6208";

    /**
     * Vérifie le code OTP de signature.
     * Dans ce mock, le code attendu est toujours "6208".
     *
     * @param code Le code saisi par l'utilisateur
     * @return true si le code est correct
     */
    public boolean verifierCode(String code) {
        log.info("Vérification du code de signature (mock)");
        return MOCK_OTP_CODE.equals(code);
    }

    /**
     * Génère un identifiant de signature unique.
     *
     * @return L'identifiant de signature
     */
    public String genererSignatureId() {
        return "SIG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Retourne l'horodatage de signature.
     *
     * @return La date/heure de signature
     */
    public LocalDateTime horodater() {
        return LocalDateTime.now();
    }
}
