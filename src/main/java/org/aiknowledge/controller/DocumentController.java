package org.aiknowledge.controller;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.dto.PageResponse;
import org.aiknowledge.dto.response.documents.DocumentResponse;
import org.aiknowledge.dto.response.documents.DocumentSummaryResponse;
import org.aiknowledge.service.DocumentService;
import org.aiknowledge.service.SecurityUserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;
    private final SecurityUserService securityUserService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        UUID userId = securityUserService.getCurrentUserId();

        DocumentResponse response = documentService.upload(userId, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<DocumentSummaryResponse>> getDocuments(
            @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = securityUserService.getCurrentUserId();
        return ResponseEntity.ok(documentService.getDocuments(userId, pageable));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable UUID documentId) {
        UUID userId = securityUserService.getCurrentUserId();

        return ResponseEntity.ok(documentService.getDocument(userId, documentId));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID documentId) {
        UUID userId = securityUserService.getCurrentUserId();
        documentService.deleteDocument(userId, documentId);

        return ResponseEntity.noContent().build();
    }
}
