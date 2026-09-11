package org.aiknowledge.processing.model;


import java.util.List;

public record ExtractedDocument(
        List<ExtractedContent> contents
) {
}
