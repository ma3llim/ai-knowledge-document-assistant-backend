package org.aiknowledge.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ConversationResponse(
        @JsonAlias("id")
        UUID conversationId,
        UUID userId,
        UUID documentId,
        String title,
        Instant createdAt,
        Instant updatedAt
) {
}
