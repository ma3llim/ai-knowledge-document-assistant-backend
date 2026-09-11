package org.aiknowledge.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DocumentFileType {
    PDF("pdf", "application/pdf"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    TXT("txt", "text/plain"),
    CSV("csv", "text/csv"),
    MARKDOWN("md", "text/markdown");

    private final String extension;
    private final String contentType;

    public static DocumentFileType fromExtension(String extension) {
        return Arrays.stream(values()).filter(type -> type.extension.equalsIgnoreCase(extension))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unsupported file extension: " + extension));
    }

    public static boolean isSupported(String extension, String contentType) {
        return Arrays.stream(values()).anyMatch(type -> type.extension.equalsIgnoreCase(extension)
                && type.contentType.equalsIgnoreCase(contentType)
        );
    }
}