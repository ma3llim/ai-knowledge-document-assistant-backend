package org.aiknowledge.extractor;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExtractedContent {
    private final String content;
    private final Integer pageNumber;
    private final String sectionName;
    private final String sheetName;
    private final Integer slideNumber;
}
