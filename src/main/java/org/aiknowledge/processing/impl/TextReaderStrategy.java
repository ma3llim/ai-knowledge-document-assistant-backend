package org.aiknowledge.processing.impl;

import org.aiknowledge.enums.DocumentType;
import org.aiknowledge.processing.DocumentReaderStrategy;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TextReaderStrategy implements DocumentReaderStrategy {
    @Override
    public boolean supports(DocumentType documentType) {
        return documentType == DocumentType.TXT;
    }

    @Override
    public List<Document> read(Resource resource) {
        return new TextReader(resource).get();
    }
}
