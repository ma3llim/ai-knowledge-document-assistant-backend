package org.aiknowledge.dto.response;

import java.util.UUID;

public record ChatStartData(
        UUID conversationId,
        String conversationTitle,
        boolean newlyCreated
) {
}
