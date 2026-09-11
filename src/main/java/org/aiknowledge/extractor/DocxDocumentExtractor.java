package org.aiknowledge.extractor;

import org.aiknowledge.enums.DocumentType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DocxDocumentExtractor implements DocumentExtractor {
    @Override
    public List<DocumentType> supports() {
        return List.of(DocumentType.DOCX);
    }

    @Override
    public List<ExtractedContent> extract(InputStream inputStream) {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            String text = document.getParagraphs().stream().map(XWPFParagraph::getText)
                    .filter(line -> line != null && !line.isBlank())
                    .reduce("", (first, second) -> first + "\n" + second);

            if (text.isBlank()) {
                return List.of();
            }

            return List.of(ExtractedContent.builder().content(text).build());
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to extract DOCX content", exception);
        }
    }
}
