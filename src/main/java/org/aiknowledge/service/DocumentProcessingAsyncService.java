package org.aiknowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.entity.Document;
import org.aiknowledge.entity.DocumentChunk;
import org.aiknowledge.enums.DocumentStatus;
import org.aiknowledge.processing.DocumentContentNormalizer;
import org.aiknowledge.processing.DocumentReaderFactory;
import org.aiknowledge.repository.DocumentChunkRepository;
import org.aiknowledge.repository.DocumentRepository;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;

    @Async("documentProcessingExecutor")
    public void processAsync(UUID documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() ->
                new IllegalArgumentException("Document not found: " + documentId));

        try {
            String objectKey = document.getR2ObjectKey();
            Resource data = objectStorageService.loadFromR2(objectKey);

            List<org.springframework.ai.document.Document> documents = documentReaderFactory.read(document.getFileType(), data);
            log.info("Document extraction completed. documentId={}, documents={}", documentId, documents.size());

            documents = documentContentNormalizer.normalize(documents);

            List<org.springframework.ai.document.Document> chunks = tokenTextSplitter.apply(documents);
            log.info("Document chunking completed. documentId={}, chunks={}", documentId, chunks.size());

            List<float[]> embeddings = embeddingService.embed(chunks);
            log.info("Document embedding completed. documentId={}, embeddings={}", documentId, embeddings.size());

            List<DocumentChunk> documentChunks = new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk documentChunk = buildDocumentChunk(documentId, i, chunks.get(i), embeddings.get(i));
                documentChunks.add(documentChunk);
            }
            documentChunkRepository.saveAll(documentChunks);

            document.setStatus(DocumentStatus.READY);
            documentRepository.save(document);

            log.info("Document chunks saved successfully. documentId={}, chunks={}", documentId, documentChunks.size());
        } catch (Exception exception) {
            log.error("Document processing failed. documentId={}", documentId, exception);
            document.setStatus(DocumentStatus.FAILED);
            documentRepository.save(document);
        }
    }

    private DocumentChunk buildDocumentChunk(UUID documentId, int chunkIndex, org.springframework.ai.document.Document chunk, float[] embedding) {
        Map<String, Object> metadata = chunk.getMetadata();
        return DocumentChunk.builder()
                .documentId(documentId)
                .chunkIndex(chunkIndex)
                .content(chunk.getText())
                .tokenCount(null)
                .pageNumber(getIntegerMetadata(metadata, "page_number"))
                .sectionName(getStringMetadata(metadata, "section_name"))
                .sheetName(getStringMetadata(metadata, "sheet_name"))
                .slideNumber(getIntegerMetadata(metadata, "slide_number"))
                .embedding(embedding)
                .build();
    }

    private String getStringMetadata(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        return value != null ? value.toString() : null;
    }

    private Integer getIntegerMetadata(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }

        return null;
    }
}
