package org.aiknowledge.integration.rag.model;

public record CitationMetadata(
        String fileName,
        String contentType,
        Integer chunkIndex,
        Integer pageNumber,
        String sectionName,
        String sheetName,
        Integer slideNumber
) {
}
