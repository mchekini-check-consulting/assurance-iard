package com.iard.repository;

import com.iard.entity.Sinistre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SinistreRepository extends JpaRepository<Sinistre, Long> {

    List<Sinistre> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Sinistre> findByIdAndUserId(Long id, Long userId);

    boolean existsByNumeroSinistre(String numeroSinistre);

    List<Sinistre> findByEventPublieFalse();

    /**
     * Marque l'événement SinistreDeclare comme publié. Requête dédiée pour être
     * utilisable depuis le callback asynchrone du producer Kafka.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Sinistre s SET s.eventPublie = true WHERE s.id = :id")
    void marquerEventPublie(@Param("id") Long id);
}
