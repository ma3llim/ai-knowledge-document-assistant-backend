package org.aiknowledge.integration.document.reader;

import org.aiknowledge.enums.DocumentType;
import org.aiknowledge.integration.document.DocumentReaderStrategy;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TikaReaderStrategy implements DocumentReaderStrategy {
    @Override
    public boolean supports(DocumentType documentType) {
        return documentType == DocumentType.DOCX || documentType == DocumentType.PPTX;
    }

    @Override
    public List<Document> read(Resource resource) {
        return new TikaDocumentReader(resource).get();
    }
}
