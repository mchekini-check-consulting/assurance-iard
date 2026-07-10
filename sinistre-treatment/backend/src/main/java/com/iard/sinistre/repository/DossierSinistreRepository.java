package com.iard.sinistre.repository;

import com.iard.sinistre.entity.DossierSinistre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DossierSinistreRepository extends JpaRepository<DossierSinistre, Long> {

    boolean existsBySinistreId(Long sinistreId);

    Optional<DossierSinistre> findBySinistreId(Long sinistreId);

    List<DossierSinistre> findAllByOrderByCreatedAtDesc();

    List<DossierSinistre> findBySyncEnAttenteTrue();

    /**
     * Marque la décision comme synchronisée (événement SinistreDecide publié).
     * Requête dédiée pour être utilisable depuis le callback asynchrone du producer Kafka.
     */
    @Modifying
    @Transactional
    @Query("UPDATE DossierSinistre d SET d.syncEnAttente = false WHERE d.id = :id")
    void marquerSynchronise(@Param("id") Long id);
}
