package org.aiknowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.entity.Document;
import org.aiknowledge.extractor.DocumentExtractor;
import org.aiknowledge.extractor.DocumentExtractorFactory;
import org.aiknowledge.processing.model.ExtractedContent;
import org.aiknowledge.repository.DocumentRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentProcessingAsyncService {
    private final DocumentRepository documentRepository;
    private final ObjectStorageService objectStorageService;
    private final DocumentExtractorFactory documentExtractorFactory;

    @Async("documentProcessingExecutor")
    public void processAsync(UUID documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() ->
                new IllegalArgumentException("Document not found: " + documentId)
        );
        String objectKey = document.getR2ObjectKey();

        try (InputStream inputStream = objectStorageService.download(objectKey)) {
            DocumentExtractor extractor = documentExtractorFactory.getExtractor(document.getFileType());

            List<ExtractedContent> extractedDocument = extractor.extract(inputStream);
            log.info(
                    "Document extraction completed, documentId={}, extractedParts={}",
                    documentId,
                    extractedDocument.size()
            );
            log.info(extractedDocument.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
