package org.aiknowledge.integration.document.reader;

import org.aiknowledge.enums.DocumentType;
import org.aiknowledge.integration.document.DocumentReaderStrategy;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class MarkdownReaderStrategy implements DocumentReaderStrategy {
    @Override
    public boolean supports(DocumentType documentType) {
        return documentType == DocumentType.MARKDOWN;
    }

    @Override
    public List<Document> read(Resource resource) {
        try {
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            return List.of(new Document(content));
        } catch (IOException exception) {
            throw new RuntimeException("Failed to read Markdown document: " + resource.getDescription(), exception);
        }
    }
}
