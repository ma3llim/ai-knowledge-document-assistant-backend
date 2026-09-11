package org.aiknowledge.extractor;

import org.aiknowledge.enums.DocumentType;

import java.io.InputStream;
import java.util.List;

public interface DocumentExtractor {
    List<DocumentType> supports();

    List<ExtractedContent> extract(InputStream inputStream);
}
