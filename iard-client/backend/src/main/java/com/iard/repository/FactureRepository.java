package com.iard.repository;

import com.iard.entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    List<Facture> findByUserIdOrderByDateEmissionDesc(Long userId);

    List<Facture> findByContratIdOrderByDateEmissionDesc(Long contratId);

    Optional<Facture> findByPaiementId(Long paiementId);

    Optional<Facture> findByIdAndUserId(Long id, Long userId);

    Optional<Facture> findByNumeroFacture(String numeroFacture);
}
