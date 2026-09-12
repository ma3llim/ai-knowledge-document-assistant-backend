package org.aiknowledge.integration.document.reader;

import org.aiknowledge.enums.DocumentType;
import org.aiknowledge.integration.document.DocumentReaderStrategy;
import org.apache.poi.ss.usermodel.*;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class XlsxReaderStrategy implements DocumentReaderStrategy {
    @Override
    public boolean supports(DocumentType documentType) {
        return documentType == DocumentType.XLSX;
    }

    @Override
    public List<Document> read(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            Workbook webhook = WorkbookFactory.create(inputStream);

            List<Document> documents = new ArrayList<>();

            DataFormatter formatter = new DataFormatter();

            for (Sheet sheet : webhook) {
                StringBuilder content = new StringBuilder();
                for (Row row : sheet) {
                    List<String> cells = new ArrayList<>();
                    for (Cell cell : row) {
                        cells.add(formatter.formatCellValue(cell));
                    }

                    content.append(String.join(" | ", cells)).append("\n");
                }

                if (!content.isEmpty()) {
                    documents.add(Document.builder()
                            .text(content.toString())
                            .metadata("sheet_name", sheet.getSheetName())
                            .build()
                    );
                }
            }
            return documents;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read XLSX document", exception);
        }
    }
}
