package org.aiknowledge.repository;

import org.aiknowledge.entity.DocumentProcessingJob;
import org.aiknowledge.enums.ProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentProcessingJobRepository extends JpaRepository<DocumentProcessingJob, UUID> {
    Optional<DocumentProcessingJob> findByDocumentId(UUID documentId);

    boolean existsByDocumentIdAndStatus(UUID documentId, ProcessingStatus status);
}
