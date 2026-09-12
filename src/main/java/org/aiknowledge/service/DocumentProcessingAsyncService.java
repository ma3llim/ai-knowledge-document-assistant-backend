package org.aiknowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.entity.Document;
import org.aiknowledge.repository.DocumentRepository;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentProcessingAsyncService {
    private final DocumentRepository documentRepository;
    private final ObjectStorageService objectStorageService;

    @Async("documentProcessingExecutor")
    public void processAsync(UUID documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() ->
                new IllegalArgumentException("Document not found: " + documentId)
        );
        String objectKey = document.getR2ObjectKey();

        Resource data = objectStorageService.loadFromR2(objectKey);

        log.info("data: {}", data);
    }
}
