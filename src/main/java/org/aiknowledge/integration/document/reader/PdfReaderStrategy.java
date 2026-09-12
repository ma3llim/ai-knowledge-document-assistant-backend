package org.aiknowledge.integration.document.reader;

import org.aiknowledge.enums.DocumentType;
import org.aiknowledge.integration.document.DocumentReaderStrategy;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PdfReaderStrategy implements DocumentReaderStrategy {
    @Override
    public boolean supports(DocumentType documentType) {
        return documentType == DocumentType.PDF;
    }

    @Override
    public List<Document> read(Resource resource) {
        return new PagePdfDocumentReader(resource).get();
    }
}
