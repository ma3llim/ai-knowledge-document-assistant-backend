package org.aiknowledge.processing.impl;

import org.aiknowledge.enums.DocumentType;
import org.aiknowledge.processing.DocumentReaderStrategy;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MarkdownReaderStrategy implements DocumentReaderStrategy {
    @Override
    public boolean supports(DocumentType documentType) {
        return documentType == DocumentType.MARKDOWN;
    }

    @Override
    public List<Document> read(Resource resource) {
        return new MarkdownDocumentReader(resource.getDescription()).get();
    }
}
