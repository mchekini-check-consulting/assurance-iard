package com.iard.sinistre.service;

import com.iard.sinistre.dto.DossierSinistreResponse;
import com.iard.sinistre.entity.DossierSinistre;
import com.iard.sinistre.entity.DossierStatutHistorique;
import com.iard.sinistre.entity.StatutDossier;
import com.iard.sinistre.event.SinistreDeclareEvent;
import com.iard.sinistre.repository.DossierSinistreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Enregistrement et consultation des dossiers de sinistres reçus de la plateforme de souscription.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DossierSinistreService {

    private final DossierSinistreRepository dossierRepository;

    /**
     * Enregistre un sinistre reçu via Kafka. Idempotent : un événement déjà
     * consommé (sinistreId existant) ne crée pas de doublon.
     */
    @Transactional
    public void enregistrerSinistre(SinistreDeclareEvent event) {
        if (event.getSinistreId() == null || event.getNumeroSinistre() == null) {
            throw new IllegalArgumentException("Événement SinistreDeclare invalide : sinistreId et numeroSinistre requis");
        }

        if (dossierRepository.existsBySinistreId(event.getSinistreId())) {
            log.info("Sinistre {} déjà enregistré, événement ignoré (idempotence)", event.getNumeroSinistre());
            return;
        }

        DossierSinistre dossier = DossierSinistre.builder()
                .sinistreId(event.getSinistreId())
                .numeroSinistre(event.getNumeroSinistre())
                .contratId(event.getContratId())
                .numeroContrat(event.getNumeroContrat())
                .userId(event.getUserId())
                .souscripteurNom(event.getSouscripteurNom())
                .souscripteurPrenom(event.getSouscripteurPrenom())
                .type(event.getType())
                .dateSinistre(event.getDateSinistre())
                .lieu(event.getLieu())
                .description(event.getDescription())
                .montantEstime(event.getMontantEstime())
                .statut(StatutDossier.A_TRAITER)
                .dateDeclaration(event.getDateDeclaration())
                .build();

        dossier.getHistoriqueStatuts().add(DossierStatutHistorique.builder()
                .dossier(dossier)
                .statut(StatutDossier.A_TRAITER)
                .commentaire("Réception de la déclaration")
                .build());

        dossierRepository.save(dossier);
        log.info("Dossier créé pour le sinistre {} (statut A_TRAITER)", event.getNumeroSinistre());
    }

    @Transactional(readOnly = true)
    public List<DossierSinistreResponse> listerDossiers() {
        return dossierRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(DossierSinistreResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DossierSinistreResponse getDossier(Long id) {
        DossierSinistre dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable"));
        return DossierSinistreResponse.fromEntity(dossier);
    }
}
