package org.aiknowledge.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ChatResponse(
        String query,
        int totalMatches,
        List<CitationResponse> matches
) {
}
