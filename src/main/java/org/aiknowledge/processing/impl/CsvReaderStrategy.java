package org.aiknowledge.processing.impl;

import org.aiknowledge.enums.DocumentType;
import org.aiknowledge.processing.DocumentReaderStrategy;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvReaderStrategy implements DocumentReaderStrategy {
    @Override
    public boolean supports(DocumentType documentType) {
        return documentType == DocumentType.CSV;
    }

    @Override
    public List<Document> read(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get();

            List<Document> documents = new ArrayList<>();

            StringBuilder content = new StringBuilder();

            for (CSVRecord record : format.parse(reader)) {
                content.append(String.join(" | ", record.values())).append("\n");
            }

            if (!content.isEmpty()) {
                documents.add(Document.builder()
                        .text(content.toString())
                        .metadata("source_type", "CSV")
                        .build()
                );
            }

            return documents;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read CSV document", exception);
        }
    }
}
