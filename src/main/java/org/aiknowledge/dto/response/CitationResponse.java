package org.aiknowledge.dto.response;

import lombok.Builder;

@Builder
public record CitationResponse(
        String fileName,
        Integer pageNumber,
        String sectionName,
        String sheetName,
        Integer slideNumber
) {
}
