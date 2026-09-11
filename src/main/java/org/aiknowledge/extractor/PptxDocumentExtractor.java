package org.aiknowledge.extractor;

import org.aiknowledge.enums.DocumentType;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class PptxDocumentExtractor implements DocumentExtractor {
    @Override
    public List<DocumentType> supports() {
        return List.of(DocumentType.PPTX);
    }

    @Override
    public List<ExtractedContent> extract(InputStream inputStream) {
        List<ExtractedContent> contents = new ArrayList<>();

        try (XMLSlideShow presentation = new XMLSlideShow(inputStream)) {
            int slideNumber = 1;

            for (var slide : presentation.getSlides()) {
                StringBuilder content = new StringBuilder();
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        String text = textShape.getText();
                        if (text != null && !text.isBlank()) {
                            content.append(text).append("\n");
                        }
                    }
                }

                if (!content.isEmpty()) {
                    contents.add(ExtractedContent.builder().content(content.toString()).slideNumber(slideNumber).build());
                }

                slideNumber++;
            }

            return contents;

        } catch (Exception exception) {
            throw new IllegalStateException("Failed to extract PPTX content", exception);
        }
    }
}
