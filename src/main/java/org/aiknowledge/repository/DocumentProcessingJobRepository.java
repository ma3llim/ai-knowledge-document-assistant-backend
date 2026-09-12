package org.aiknowledge.repository;

import org.aiknowledge.entity.DocumentProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentProcessingJobRepository extends JpaRepository<DocumentProcessingJob, UUID> {
    Optional<DocumentProcessingJob> findTopByDocumentIdOrderByCreatedAtDesc(UUID documentId);
}
