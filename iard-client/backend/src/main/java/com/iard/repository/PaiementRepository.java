package com.iard.repository;

import com.iard.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findByContratIdOrderByCreatedAtDesc(Long contratId);

    Optional<Paiement> findByContratIdAndPeriode(Long contratId, String periode);

    boolean existsByContratIdAndPeriode(Long contratId, String periode);

    List<Paiement> findByContratUserIdOrderByCreatedAtDesc(Long userId);
}
