package org.aiknowledge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.dto.PageResponse;
import org.aiknowledge.dto.response.documents.DocumentResponse;
import org.aiknowledge.dto.response.documents.DocumentSummaryResponse;
import org.aiknowledge.entity.Document;
import org.aiknowledge.enums.DocumentStatus;
import org.aiknowledge.enums.DocumentType;
import org.aiknowledge.exception.FileStorageException;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.repository.DocumentRepository;
import org.aiknowledge.validation.DocumentFileValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final ObjectStorageService objectStorageService;
    private final DocumentFileValidator documentFileValidator;
    private final ObjectMapper objectMapper;

    @Transactional
    public DocumentResponse upload(UUID userId, MultipartFile file) {
        DocumentType documentType = documentFileValidator.validate(file);

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null) {
            throw new IllegalArgumentException("Invalid filename");
        }

        UUID documentId = UUID.randomUUID();

        String objectKey = buildObjectKey(userId, documentId, originalFilename);

        boolean uploadedToR2 = false;
        try {
            objectStorageService.upload(objectKey, file);

            uploadedToR2 = true;

            Document document = Document.builder()
                    .id(documentId)
                    .userId(userId)
                    .originalFilename(originalFilename)
                    .fileType(documentType)
                    .fileSize(file.getSize())
                    .r2ObjectKey(objectKey)
                    .status(DocumentStatus.UPLOADED)
                    .build();

            document = documentRepository.save(document);

            log.info("Document uploaded successfully, documentId={}, userId={}", documentId, userId);

            return objectMapper.convertValue(document, DocumentResponse.class);
        } catch (IOException | RuntimeException exception) {
            log.error("Document upload failed, documentId={}, userId={}", documentId, userId, exception);
            if (uploadedToR2) {
                try {
                    objectStorageService.delete(objectKey);
                    log.info("R2 object cleanup completed after upload failure, objectKey={}", objectKey);
                } catch (RuntimeException cleanupException) {
                    log.error("CRITICAL: R2 cleanup failed after database/upload failure, objectKey={}",
                            objectKey, cleanupException);

                }
            }
            throw new FileStorageException("Document upload failed", exception);
        }
    }

    public PageResponse<DocumentSummaryResponse> getDocuments(UUID userId, Pageable pageable) {
        Page<Document> documents = documentRepository.findAllByUserId(userId, pageable);

        List<DocumentSummaryResponse> content = documents.getContent().stream()
                .map(document -> objectMapper.convertValue(document, DocumentSummaryResponse.class))
                .toList();

        return PageResponse.<DocumentSummaryResponse>builder()
                .content(content)
                .page(documents.getNumber())
                .size(documents.getSize())
                .totalElements(documents.getTotalElements())
                .totalPages(documents.getTotalPages())
                .first(documents.isFirst())
                .last(documents.isLast())
                .build();
    }

    public DocumentResponse getDocument(UUID userId, UUID documentId) {
        Document document = documentRepository.findByIdAndUserId(documentId, userId).orElseThrow(() -> {
            log.warn("Fetch document failed: document not found. documentId={}, userId={}", documentId, userId);
            return new ResourceNotFoundException("Document not found");
        });

        return objectMapper.convertValue(document, DocumentResponse.class);
    }

    @Transactional
    public void deleteDocument(UUID userId, UUID documentId) {
        Document document = documentRepository.findByIdAndUserId(documentId, userId).orElseThrow(() ->
                new ResourceNotFoundException("Document not found"));

        document.setStatus(DocumentStatus.DELETING);

        documentRepository.save(document);

        try {
            objectStorageService.delete(document.getR2ObjectKey());

            documentRepository.delete(document);

            log.info("Document deleted successfully, documentId={}, userId={}", documentId, userId);

        } catch (RuntimeException exception) {
            document.setStatus(DocumentStatus.FAILED);
            document.setFailureReason("Failed to delete document from storage");

            documentRepository.save(document);

            log.error("Document deletion failed, documentId={}, userId={}", documentId, userId, exception);

            throw new FileStorageException("Document deletion failed", exception);
        }
    }

    private String buildObjectKey(UUID userId, UUID documentId, String filename) {
        return "users/" + userId + "/documents/" + documentId + "/" + filename;
    }
}
