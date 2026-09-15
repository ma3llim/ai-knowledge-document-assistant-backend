package org.aiknowledge.dto.response;

import java.util.List;

public record ChatApiResponse(
        String answer,
        List<CitationResponse> citations
) {
}
