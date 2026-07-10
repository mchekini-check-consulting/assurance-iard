package com.iard.repository;

import com.iard.entity.Contrat;
import com.iard.entity.StatutContrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {

    List<Contrat> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Contrat> findByUserIdAndStatutOrderByCreatedAtDesc(Long userId, StatutContrat statut);

    Optional<Contrat> findByIdAndUserId(Long id, Long userId);

    Optional<Contrat> findByDevisId(Long devisId);

    Optional<Contrat> findByNumeroContrat(String numeroContrat);

    boolean existsByDevisId(Long devisId);

    long countByUserIdAndStatut(Long userId, StatutContrat statut);

    /**
     * Trouve les contrats éligibles au prélèvement pour une date donnée.
     * Éligibles : contrats signés (EN_ATTENTE ou ACTIF) avec prochaineDatePrelevement = date
     */
    @Query("SELECT c FROM Contrat c WHERE c.prochaineDatePrelevement = :date " +
           "AND c.statut IN :statuts AND c.dateSignature IS NOT NULL")
    List<Contrat> findContratsEligiblesPrelevement(
            @Param("date") LocalDate date,
            @Param("statuts") List<StatutContrat> statuts);
}
