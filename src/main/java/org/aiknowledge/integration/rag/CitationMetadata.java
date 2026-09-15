package org.aiknowledge.integration.rag;

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
