package org.aiknowledge.extractor;

import org.aiknowledge.enums.DocumentType;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvDocumentExtractor implements DocumentExtractor {

    @Override
    public List<DocumentType> supports() {
        return List.of(DocumentType.CSV);
    }

    @Override
    public List<ExtractedContent> extract(InputStream inputStream) {

        try {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            if (content.isBlank()) {
                return List.of();
            }

            return List.of(ExtractedContent.builder().content(content).build());

        } catch (Exception exception) {
            throw new IllegalStateException("Failed to extract CSV content", exception);
        }
    }
}