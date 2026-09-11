package org.aiknowledge.dto.response.documents;

import lombok.Builder;
import org.aiknowledge.enums.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

@Builder
public record DocumentResponse(
        UUID id,
        String originalFilename,
        String fileType,
        Long fileSize,
        DocumentStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt,
        Instant processedAt
) {
}