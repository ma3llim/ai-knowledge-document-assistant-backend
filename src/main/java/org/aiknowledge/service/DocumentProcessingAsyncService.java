package org.aiknowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.entity.Document;
import org.aiknowledge.processing.DocumentContentNormalizer;
import org.aiknowledge.processing.DocumentReaderFactory;
import org.aiknowledge.repository.DocumentRepository;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentProcessingAsyncService {
    private final DocumentRepository documentRepository;
    private final ObjectStorageService objectStorageService;
    private final DocumentReaderFactory documentReaderFactory;
    private final TokenTextSplitter tokenTextSplitter;
    private final DocumentContentNormalizer documentContentNormalizer;

    @Async("documentProcessingExecutor")
    public void processAsync(UUID documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() ->
                new IllegalArgumentException("Document not found: " + documentId));

        String objectKey = document.getR2ObjectKey();
        Resource data = objectStorageService.loadFromR2(objectKey);

        List<org.springframework.ai.document.Document> documents = documentReaderFactory.read(document.getFileType(), data);

        documents = documentContentNormalizer.normalize(documents);

        List<org.springframework.ai.document.Document> chunks = tokenTextSplitter.apply(documents);

    }
}
