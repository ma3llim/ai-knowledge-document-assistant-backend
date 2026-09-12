package org.aiknowledge.processing;

import org.aiknowledge.enums.DocumentType;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;

public interface DocumentReaderStrategy {
    boolean supports(DocumentType documentType);

    List<Document> read(Resource resource);
}
