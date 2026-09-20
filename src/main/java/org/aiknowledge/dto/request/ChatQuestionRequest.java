package org.aiknowledge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ChatQuestionRequest(
        @NotNull(message = "Document ID is required")
        UUID documentId,

        UUID userId,
        UUID conversationId,

        @NotBlank(message = "User query is required")
        @Size(max = 2000, message = "User query must not exceed 2000 characters")
        String userQuery
) {
}
