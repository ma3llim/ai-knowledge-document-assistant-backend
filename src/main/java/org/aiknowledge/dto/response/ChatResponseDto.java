package org.aiknowledge.dto.response;

import java.util.List;

public record ChatResponseDto(
        String answer,
        List<CitationReference> citations
) {
}
