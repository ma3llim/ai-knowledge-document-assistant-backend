package org.aiknowledge.extractor;

import org.aiknowledge.enums.DocumentType;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class XlsxDocumentExtractor implements DocumentExtractor {
    @Override
    public List<DocumentType> supports() {
        return List.of(DocumentType.XLSX);
    }

    @Override
    public List<ExtractedContent> extract(InputStream inputStream) {
        List<ExtractedContent> contents = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            DataFormatter formatter = new DataFormatter();

            for (Sheet sheet : workbook) {
                StringBuilder content = new StringBuilder();
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        String value = formatter.formatCellValue(cell);

                        if (!value.isBlank()) {
                            content.append(value).append(" ");
                        }
                    }
                    content.append("\n");
                }
                if (!content.isEmpty() && !content.toString().isBlank()) {
                    contents.add(ExtractedContent.builder()
                            .content(content.toString())
                            .sheetName(sheet.getSheetName())
                            .build());
                }
            }
            return contents;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to extract XLSX content", exception);
        }
    }
}
