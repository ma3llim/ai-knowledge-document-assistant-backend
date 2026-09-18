package org.aiknowledge.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record MessageResponse(
        UUID id,
        UUID conversationId,
        String role,
        String content,
        Instant createdAt
) {
}
