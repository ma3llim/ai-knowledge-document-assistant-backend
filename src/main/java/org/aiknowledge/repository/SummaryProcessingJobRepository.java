package org.aiknowledge.repository;

import org.aiknowledge.entity.SummaryProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SummaryProcessingJobRepository extends JpaRepository<SummaryProcessingJob, UUID> {

    Optional<SummaryProcessingJob> findTopByDocumentIdOrderByCreatedAtDesc(UUID documentId);
}