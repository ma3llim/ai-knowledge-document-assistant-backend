package org.aiknowledge.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateConversationTitleRequest(
        @NotBlank
        String title
) {
}
