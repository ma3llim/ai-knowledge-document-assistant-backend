package org.aiknowledge.validation;

import org.aiknowledge.enums.DocumentFileType;
import org.aiknowledge.enums.DocumentType;
import org.aiknowledge.exception.InvalidDocumentException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class DocumentFileValidator {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public DocumentType validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidDocumentException("File size must not exceed 10 MB");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new InvalidDocumentException("Invalid filename");
        }

        String extension = getExtension(filename);
        String contentType = file.getContentType();

        if (contentType == null || !DocumentFileType.isSupported(extension, contentType)) {
            throw new InvalidDocumentException("Unsupported file type");
        }

        return DocumentType.valueOf(DocumentFileType.fromExtension(extension).name());
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }
}
