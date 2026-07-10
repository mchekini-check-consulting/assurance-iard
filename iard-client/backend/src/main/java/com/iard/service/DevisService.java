package com.iard.service;

import com.iard.dto.DevisRequest;
import com.iard.dto.DevisResponse;
import com.iard.dto.TarificationRequest;
import com.iard.entity.*;
import com.iard.repository.DevisRepository;
import com.iard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DevisService {

    private final DevisRepository devisRepository;
    private final UserRepository userRepository;
    private final TarificationService tarificationService;

    @Transactional
    public DevisResponse creerDevis(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        Devis devis = Devis.builder()
                .user(user)
                .produit(Produit.HABITATION)
                .statut(StatutDevis.BROUILLON)
                .etapeCourante(1)
                .build();

        devis = devisRepository.save(devis);
        return toResponse(devis);
    }

    @Transactional
    public DevisResponse sauvegarderEtape(Long devisId, Long userId, DevisRequest request) {
        Devis devis = devisRepository.findByIdAndUserId(devisId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Devis non trouvé"));

        // Mettre à jour les données du risque
        DonneesRisqueHabitation donnees = devis.getDonneesRisque();
        if (donnees == null) {
            donnees = new DonneesRisqueHabitation();
        }

        // Mapper les champs selon l'étape
        updateDonneesRisque(donnees, request);
        devis.setDonneesRisque(donnees);

        // Mettre à jour l'étape courante
        if (request.getEtapeCourante() != null) {
            devis.setEtapeCourante(request.getEtapeCourante());
        }

        // Gérer l'assuré
        if (Boolean.FALSE.equals(request.getSouscripteurEstAssure()) && request.getAssure() != null) {
            PersonneAssuree assure = PersonneAssuree.builder()
                    .civilite(request.getAssure().getCivilite())
                    .prenom(request.getAssure().getPrenom())
                    .nom(request.getAssure().getNom())
                    .email(request.getAssure().getEmail())
                    .telephone(request.getAssure().getTelephone())
                    .adresse(request.getAssure().getAdresse())
                    .codePostal(request.getAssure().getCodePostal())
                    .ville(request.getAssure().getVille())
                    .build();
            devis.setAssure(assure);
        } else {
            devis.setAssure(null);
        }

        devis = devisRepository.save(devis);
        return toResponse(devis);
    }

    @Transactional
    public DevisResponse calculerTarif(Long devisId, Long userId) {
        Devis devis = devisRepository.findByIdAndUserId(devisId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Devis non trouvé"));

        DonneesRisqueHabitation donnees = devis.getDonneesRisque();
        if (donnees == null || donnees.getFormule() == null) {
            throw new IllegalArgumentException("Données du devis incomplètes");
        }

        // Construire la requête de tarification
        TarificationRequest tarifRequest = TarificationRequest.builder()
                .typeBien(donnees.getTypeBien())
                .typeResidence(donnees.getTypeResidence())
                .codePostal(donnees.getCodePostal())
                .surfaceHabitable(donnees.getSurfaceHabitable())
                .nombrePieces(donnees.getNombrePieces())
                .etage(donnees.getEtage())
                .anneeConstruction(donnees.getAnneeConstruction())
                .statutOccupation(donnees.getStatutOccupation())
                .alarme(donnees.getAlarme())
                .porteBlindee(donnees.getPorteBlindee())
                .dependances(donnees.getDependances())
                .surfaceDependances(donnees.getSurfaceDependances())
                .piscine(donnees.getPiscine())
                .capitalMobilier(donnees.getCapitalMobilier())
                .objetsValeur(donnees.getObjetsValeur())
                .valeurObjetsValeur(donnees.getValeurObjetsValeur())
                .nombreSinistres36Mois(donnees.getNombreSinistres36Mois())
                .formule(donnees.getFormule())
                .optionsGaranties(donnees.getOptionsGaranties())
                .build();

        // Appeler le service de tarification
        ResultatTarification resultat = tarificationService.calculerTarif(tarifRequest);

        // Mettre à jour le devis
        devis.setResultatTarif(resultat);
        devis.setStatut(StatutDevis.DEVIS);

        devis = devisRepository.save(devis);
        return toResponse(devis);
    }

    @Transactional(readOnly = true)
    public List<DevisResponse> listerDevis(Long userId) {
        return devisRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DevisResponse getDevis(Long devisId, Long userId) {
        Devis devis = devisRepository.findByIdAndUserId(devisId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Devis non trouvé"));
        return toResponse(devis);
    }

    @Transactional
    public void supprimerDevis(Long devisId, Long userId) {
        Devis devis = devisRepository.findByIdAndUserId(devisId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Devis non trouvé"));
        devisRepository.delete(devis);
    }

    @Transactional(readOnly = true)
    public long compterDevisBrouillon(Long userId) {
        return devisRepository.countByUserIdAndStatut(userId, StatutDevis.BROUILLON);
    }

    @Transactional(readOnly = true)
    public long compterDevisTermines(Long userId) {
        return devisRepository.countByUserIdAndStatut(userId, StatutDevis.DEVIS);
    }

    private void updateDonneesRisque(DonneesRisqueHabitation donnees, DevisRequest request) {
        // Étape 1 : Le bien
        if (request.getTypeBien() != null) donnees.setTypeBien(request.getTypeBien());
        if (request.getTypeResidence() != null) donnees.setTypeResidence(request.getTypeResidence());
        if (request.getAdresse() != null) donnees.setAdresse(request.getAdresse());
        if (request.getCodePostal() != null) donnees.setCodePostal(request.getCodePostal());
        if (request.getVille() != null) donnees.setVille(request.getVille());
        if (request.getSurfaceHabitable() != null) donnees.setSurfaceHabitable(request.getSurfaceHabitable());
        if (request.getNombrePieces() != null) donnees.setNombrePieces(request.getNombrePieces());
        if (request.getEtage() != null) donnees.setEtage(request.getEtage());
        if (request.getAnneeConstruction() != null) donnees.setAnneeConstruction(request.getAnneeConstruction());

        // Étape 2 : L'occupation
        if (request.getStatutOccupation() != null) donnees.setStatutOccupation(request.getStatutOccupation());

        // Étape 3 : Sécurité
        if (request.getAlarme() != null) donnees.setAlarme(request.getAlarme());
        if (request.getPorteBlindee() != null) donnees.setPorteBlindee(request.getPorteBlindee());
        if (request.getDependances() != null) donnees.setDependances(request.getDependances());
        if (request.getSurfaceDependances() != null) donnees.setSurfaceDependances(request.getSurfaceDependances());
        if (request.getPiscine() != null) donnees.setPiscine(request.getPiscine());

        // Étape 4 : Le contenu
        if (request.getCapitalMobilier() != null) donnees.setCapitalMobilier(request.getCapitalMobilier());
        if (request.getObjetsValeur() != null) donnees.setObjetsValeur(request.getObjetsValeur());
        if (request.getValeurObjetsValeur() != null) donnees.setValeurObjetsValeur(request.getValeurObjetsValeur());

        // Étape 5 : Antécédents
        if (request.getNombreSinistres36Mois() != null) donnees.setNombreSinistres36Mois(request.getNombreSinistres36Mois());

        // Étape 6 : Formule
        if (request.getFormule() != null) donnees.setFormule(request.getFormule());
        if (request.getOptionsGaranties() != null) donnees.setOptionsGaranties(request.getOptionsGaranties());
    }

    private DevisResponse toResponse(Devis devis) {
        DevisResponse.DevisResponseBuilder builder = DevisResponse.builder()
                .id(devis.getId())
                .produit(devis.getProduit())
                .statut(devis.getStatut())
                .etapeCourante(devis.getEtapeCourante())
                .donneesRisque(devis.getDonneesRisque())
                .assure(devis.getAssure())
                .resultatTarif(devis.getResultatTarif())
                .createdAt(devis.getCreatedAt())
                .updatedAt(devis.getUpdatedAt());

        // Infos résumées
        if (devis.getDonneesRisque() != null) {
            DonneesRisqueHabitation donnees = devis.getDonneesRisque();
            if (donnees.getAdresse() != null && donnees.getCodePostal() != null) {
                builder.adresseResume(donnees.getAdresse() + ", " + donnees.getCodePostal() +
                        (donnees.getVille() != null ? " " + donnees.getVille() : ""));
            }
            if (donnees.getFormule() != null) {
                builder.formuleResume(donnees.getFormule().name());
            }
        }

        if (devis.getResultatTarif() != null) {
            builder.primeTTCResume(devis.getResultatTarif().getPrimeTTC().toString() + " €/an");
        }

        return builder.build();
    }
}
