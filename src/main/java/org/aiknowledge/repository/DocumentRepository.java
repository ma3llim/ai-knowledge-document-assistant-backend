package org.aiknowledge.repository;

import org.aiknowledge.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Page<Document> findAllByUserId(UUID userId, Pageable pageable);

    Optional<Document> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
                SELECT d.fileName FROM Document d WHERE d.id = :documentId AND d.user.id = :userId
            """)
    Optional<String> findFileNameByIdAndUserId(UUID documentId, UUID userId);
}
