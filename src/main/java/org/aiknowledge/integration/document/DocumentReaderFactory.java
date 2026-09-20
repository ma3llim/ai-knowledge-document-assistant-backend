package org.aiknowledge.integration.document;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.enums.DocumentType;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentReaderFactory {
    private final List<DocumentReaderStrategy> strategies;

    public List<Document> read(DocumentType documentType, Resource resource) {
        return strategies.stream()
                .filter(strategies -> strategies.supports(documentType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No document reader found for type: " + documentType))
                .read(resource);
    }
}
