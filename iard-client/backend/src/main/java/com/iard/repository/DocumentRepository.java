package com.iard.repository;

import com.iard.entity.KycDocument;
import com.iard.entity.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<KycDocument, Long> {

    List<KycDocument> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<KycDocument> findByUserIdAndType(Long userId, TypeDocument type);

    Optional<KycDocument> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndType(Long userId, TypeDocument type);
}
