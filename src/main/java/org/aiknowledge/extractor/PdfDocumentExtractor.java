package org.aiknowledge.extractor;

import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.enums.DocumentType;
import org.aiknowledge.exception.FileStorageException;
import org.aiknowledge.processing.model.ExtractedContent;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PdfDocumentExtractor implements DocumentExtractor {
    @Override
    public List<ExtractedContent> extract(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();

            try (var document = Loader.loadPDF(bytes)) {
                int pageCount = document.getNumberOfPages();

                PDFTextStripper stripper = new PDFTextStripper();

                List<ExtractedContent> contents = new ArrayList<>();

                for (int page = 1; page <= pageCount; page++) {
                    stripper.setStartPage(page);
                    stripper.setEndPage(page);

                    String text = stripper.getText(document);

                    if (text != null && !text.isBlank()) {
                        contents.add(
                                ExtractedContent.builder()
                                        .content(text)
                                        .pageNumber(page)
                                        .build()
                        );
                    }
                }
                return contents;
            }
        } catch (IOException exception) {
            throw new FileStorageException("Failed to extract PDF content", exception);
        }
    }

    @Override
    public List<DocumentType> supports() {
        return List.of(DocumentType.PDF);
    }
}
