package org.aiknowledge.dto.response;

import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record CitationResponse(
        UUID documentId,
        String fileName,
        String chunkIndex,
        String pageNumber,
        String snippet,
        String SimilarityScore,
        Map<String, Object> metadata
) {
}
