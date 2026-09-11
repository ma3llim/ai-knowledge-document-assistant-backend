package org.aiknowledge.dto.response.documents;

import lombok.Builder;
import org.aiknowledge.enums.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

@Builder
public record DocumentSummaryResponse(
        UUID id,
        String originalFilename,
        String fileType,
        Long fileSize,
        DocumentStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
