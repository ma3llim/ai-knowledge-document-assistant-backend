package org.aiknowledge.extractor;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.enums.DocumentType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentExtractorFactory {
    private final List<DocumentExtractor> extractors;

    public DocumentExtractor getExtractor(DocumentType documentType) {
        return extractors.stream()
                .filter(extractor -> extractor.supports().contains(documentType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No extractor found for document type: " + documentType));
    }
}
