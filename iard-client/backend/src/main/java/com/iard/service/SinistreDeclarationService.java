package com.iard.service;

import com.iard.dto.DeclarationSinistreRequest;
import com.iard.dto.SinistreResponse;
import com.iard.entity.*;
import com.iard.repository.ContratRepository;
import com.iard.repository.SinistreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Validation des règles métier de déclaration d'un sinistre habitation.
 * Service dédié pour pouvoir être enrichi plus tard.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SinistreDeclarationService {

    private static final int MAX_PIECES_JOINTES = 5;
    private static final long MAX_TAILLE_FICHIER = 5 * 1024 * 1024; // 5 Mo
    private static final Set<String> CONTENT_TYPES_AUTORISES =
            Set.of("image/jpeg", "image/png", "application/pdf");

    private final SinistreRepository sinistreRepository;
    private final ContratRepository contratRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${app.sinistre.upload-dir:./uploads/sinistres}")
    private String uploadDir;

    @Transactional
    public SinistreResponse declarerSinistre(Long userId, DeclarationSinistreRequest request,
                                             List<MultipartFile> fichiers) {
        Contrat contrat = contratRepository.findByIdAndUserId(request.getContratId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Contrat introuvable"));

        if (contrat.getProduit() != Produit.HABITATION) {
            throw new IllegalArgumentException("Un sinistre ne peut être déclaré que sur un contrat habitation");
        }
        if (contrat.getStatut() != StatutContrat.ACTIF) {
            throw new IllegalArgumentException("Un sinistre ne peut être déclaré que sur un contrat actif");
        }

        if (request.getDateSinistre().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La date du sinistre ne peut pas être postérieure à la date du jour");
        }
        LocalDate dateEffet = contrat.getDateSignature() != null
                ? contrat.getDateSignature().toLocalDate()
                : contrat.getCreatedAt().toLocalDate();
        if (request.getDateSinistre().isBefore(dateEffet)) {
            throw new IllegalArgumentException(
                    "La date du sinistre ne peut pas être antérieure à la date d'effet du contrat (" + dateEffet + ")");
        }

        if (request.getMontantEstime() != null && request.getMontantEstime().signum() < 0) {
            throw new IllegalArgumentException("Le montant estimé ne peut pas être négatif");
        }

        validerFichiers(fichiers);

        Sinistre sinistre = Sinistre.builder()
                .numeroSinistre(genererNumeroSinistre())
                .contrat(contrat)
                .user(contrat.getUser())
                .type(request.getType())
                .dateSinistre(request.getDateSinistre())
                .lieu(request.getLieu().trim())
                .description(request.getDescription().trim())
                .montantEstime(request.getMontantEstime())
                .statut(StatutSinistre.DECLARE)
                .build();

        sinistre.getHistoriqueStatuts().add(SinistreStatutHistorique.builder()
                .sinistre(sinistre)
                .statut(StatutSinistre.DECLARE)
                .build());

        sinistre = sinistreRepository.save(sinistre);
        enregistrerPiecesJointes(sinistre, fichiers);
        sinistre = sinistreRepository.save(sinistre);

        log.info("Sinistre {} déclaré (contrat {}, user {})",
                sinistre.getNumeroSinistre(), contrat.getNumeroContrat(), userId);

        // Publication Kafka après commit (AFTER_COMMIT) pour ne jamais publier un sinistre non persisté
        applicationEventPublisher.publishEvent(new SinistreCreeEvent(sinistre.getId()));

        return SinistreResponse.fromEntity(sinistre);
    }

    @Transactional(readOnly = true)
    public List<SinistreResponse> listerSinistres(Long userId) {
        return sinistreRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(SinistreResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SinistreResponse getSinistre(Long id, Long userId) {
        Sinistre sinistre = sinistreRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Sinistre introuvable"));
        return SinistreResponse.fromEntity(sinistre);
    }

    @Transactional(readOnly = true)
    public SinistrePieceJointe getPieceJointe(Long sinistreId, Long pieceId, Long userId) {
        Sinistre sinistre = sinistreRepository.findByIdAndUserId(sinistreId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Sinistre introuvable"));
        return sinistre.getPiecesJointes().stream()
                .filter(pj -> pj.getId().equals(pieceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Pièce jointe introuvable"));
    }

    private void validerFichiers(List<MultipartFile> fichiers) {
        if (fichiers == null || fichiers.isEmpty()) {
            return;
        }
        if (fichiers.size() > MAX_PIECES_JOINTES) {
            throw new IllegalArgumentException("Maximum " + MAX_PIECES_JOINTES + " pièces jointes autorisées");
        }
        for (MultipartFile fichier : fichiers) {
            if (fichier.getSize() > MAX_TAILLE_FICHIER) {
                throw new IllegalArgumentException(
                        "Le fichier " + fichier.getOriginalFilename() + " dépasse la taille maximale de 5 Mo");
            }
            if (fichier.getContentType() == null || !CONTENT_TYPES_AUTORISES.contains(fichier.getContentType())) {
                throw new IllegalArgumentException(
                        "Format non autorisé pour " + fichier.getOriginalFilename() + " (jpg, png ou pdf uniquement)");
            }
        }
    }

    private void enregistrerPiecesJointes(Sinistre sinistre, List<MultipartFile> fichiers) {
        if (fichiers == null || fichiers.isEmpty()) {
            return;
        }
        Path dossier = Paths.get(uploadDir, "sinistre-" + sinistre.getId());
        try {
            Files.createDirectories(dossier);
            for (MultipartFile fichier : fichiers) {
                String nomFichier = fichier.getOriginalFilename() != null
                        ? Paths.get(fichier.getOriginalFilename()).getFileName().toString()
                        : "piece-jointe";
                Path cible = dossier.resolve(UUID.randomUUID() + "_" + nomFichier);
                Files.copy(fichier.getInputStream(), cible, StandardCopyOption.REPLACE_EXISTING);
                sinistre.getPiecesJointes().add(SinistrePieceJointe.builder()
                        .sinistre(sinistre)
                        .nomFichier(nomFichier)
                        .path(cible.toString())
                        .contentType(fichier.getContentType())
                        .build());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Erreur lors de l'enregistrement des pièces jointes", e);
        }
    }

    private String genererNumeroSinistre() {
        int annee = Year.now().getValue();
        long sequence = sinistreRepository.count() + 1;
        String numero;
        do {
            numero = String.format("SIN-%d-%06d", annee, sequence++);
        } while (sinistreRepository.existsByNumeroSinistre(numero));
        return numero;
    }

    /**
     * Événement Spring interne déclenchant la publication Kafka après commit.
     */
    public record SinistreCreeEvent(Long sinistreId) {
    }
}
