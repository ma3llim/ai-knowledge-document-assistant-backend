package org.aiknowledge.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum DocumentFileType {
    PDF("pdf", Set.of("application/pdf")),
    DOCX("docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
    XLSX("xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
    PPTX("pptx", Set.of("application/vnd.openxmlformats-officedocument.presentationml.presentation")),
    TXT("txt", Set.of("text/plain")),
    CSV("csv", Set.of("text/csv", "application/vnd.ms-excel")),
    MARKDOWN("md", Set.of("text/markdown", "text/plain", "application/octet-stream"));

    private final String extension;
    private final Set<String> contentTypes;

    public static DocumentFileType fromExtension(String extension) {
        return Arrays.stream(values())
                .filter(type -> type.extension.equalsIgnoreCase(extension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported file extension: " + extension));
    }

    public static boolean isSupported(String extension, String contentType) {
        return Arrays.stream(values()).anyMatch(type ->
                type.extension.equalsIgnoreCase(extension) && type.contentTypes.stream().anyMatch(contentType::equalsIgnoreCase)
        );
    }
}