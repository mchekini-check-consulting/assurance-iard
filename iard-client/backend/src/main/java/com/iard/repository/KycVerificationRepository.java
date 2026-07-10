package com.iard.repository;

import com.iard.entity.KycVerification;
import com.iard.entity.StatutKyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycVerificationRepository extends JpaRepository<KycVerification, Long> {

    Optional<KycVerification> findByUserId(Long userId);

    boolean existsByUserIdAndStatut(Long userId, StatutKyc statut);
}
