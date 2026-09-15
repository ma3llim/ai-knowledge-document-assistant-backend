package org.aiknowledge.dto.response;

import java.util.List;
import java.util.UUID;

public record ChatApiResponse(
        UUID conversationId,
        UUID messageId,
        String answer,
        List<CitationResponse> citations
) {
}
