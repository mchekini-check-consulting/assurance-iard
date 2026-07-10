package com.iard.repository;

import com.iard.entity.Devis;
import com.iard.entity.Produit;
import com.iard.entity.StatutDevis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DevisRepository extends JpaRepository<Devis, Long> {

    List<Devis> findByUserIdOrderByUpdatedAtDesc(Long userId);

    List<Devis> findByUserIdAndStatutOrderByUpdatedAtDesc(Long userId, StatutDevis statut);

    List<Devis> findByUserIdAndProduitOrderByUpdatedAtDesc(Long userId, Produit produit);

    Optional<Devis> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndStatut(Long userId, StatutDevis statut);
}
