package org.aiknowledge.integration.document.reader;

import org.aiknowledge.enums.DocumentType;
import org.aiknowledge.integration.document.DocumentReaderStrategy;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TikaReaderStrategy implements DocumentReaderStrategy {
    @Override
    public boolean supports(DocumentType documentType) {
        return documentType == DocumentType.DOCX || documentType == DocumentType.PPTX;
    }

    @Override
    public List<Document> read(Resource resource) {
        List<Document> documents = new TikaDocumentReader(resource).get();
        return documents.stream().map(document -> {
            Map<String, Object> metadata = new HashMap<>(document.getMetadata());

            metadata.remove("source");

            return new Document(document.getText(), metadata);
        }).toList();
    }
}
