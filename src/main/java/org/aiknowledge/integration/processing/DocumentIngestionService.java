package org.aiknowledge.integration.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.entity.Document;
import org.aiknowledge.entity.DocumentProcessingJob;
import org.aiknowledge.enums.DocumentStatus;
import org.aiknowledge.enums.ProcessingErrorCode;
import org.aiknowledge.enums.ProcessingStatus;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.integration.document.DocumentContentNormalizer;
import org.aiknowledge.integration.document.DocumentReaderFactory;
import org.aiknowledge.integration.storage.ObjectStorageService;
import org.aiknowledge.repository.DocumentProcessingJobRepository;
import org.aiknowledge.repository.DocumentRepository;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentIngestionService {
    private final DocumentRepository documentRepository;
    private final ObjectStorageService objectStorageService;
    private final DocumentReaderFactory documentReaderFactory;
    private final DocumentContentNormalizer documentContentNormalizer;
    private final DocumentProcessingJobRepository documentProcessingJobRepository;
    private final VectorStore vectorStore;
    private final AppProperties properties;

    public void process(UUID jobId) {
        DocumentProcessingJob job = documentProcessingJobRepository.findById(jobId).orElseThrow(() ->
                new ResourceNotFoundException("Processing job not found: " + jobId));


        Document document = documentRepository.findById(job.getDocumentId()).orElseThrow(() ->
                new ResourceNotFoundException("Document not found: " + job.getDocumentId()));

        try {
            job.setStatus(ProcessingStatus.PROCESSING);
            job.setStartedAt(Instant.now());
            job.setAttemptCount(job.getAttemptCount() + 1);

            documentProcessingJobRepository.save(job);

            String objectKey = document.getR2ObjectKey();
            Resource data = objectStorageService.loadFromR2(objectKey);

            List<org.springframework.ai.document.Document> documents = documentReaderFactory.read(document.getFileType(), data);

            documents = documentContentNormalizer.normalize(documents);

            TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
                    .withChunkSize(properties.ai().rag().chunkSize())
                    .withMinChunkSizeChars(properties.ai().rag().minChunkCharacters())
                    .withMinChunkLengthToEmbed(properties.ai().rag().minChunkLengthToEmbed())
                    .withMaxNumChunks(properties.ai().rag().maxChunkSize())
                    .withKeepSeparator(true)
                    .build();

            List<org.springframework.ai.document.Document> chunks = tokenTextSplitter.apply(documents);

            if (chunks.isEmpty()) {
                document.setStatus(DocumentStatus.FAILED);
                document.setFailureReason("Document is empty or its content could not be extracted.");
                return;
            }

            List<org.springframework.ai.document.Document> enrichedChunks = new ArrayList<>();

            for (int chunkIndex = 0; chunkIndex < chunks.size(); chunkIndex++) {
                org.springframework.ai.document.Document chunk = chunks.get(chunkIndex);

                Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
                metadata.put("document_id", document.getId().toString());
                metadata.put("file_name", document.getOriginalFilename());
                metadata.put("content_type", document.getFileType());
                metadata.put("chunk_index", chunkIndex);

                addSourceMetadata(metadata, chunk);

                enrichedChunks.add(new org.springframework.ai.document.Document(chunk.getText(), metadata));
            }

            vectorStore.add(enrichedChunks);

            document.setStatus(DocumentStatus.READY);
            document.setProcessedAt(Instant.now());
            documentRepository.save(document);

            job.setStatus(ProcessingStatus.COMPLETED);
            job.setCompletedAt(Instant.now());
            documentProcessingJobRepository.save(job);
            log.info("Document chunks embedded and stored successfully. documentId={}, chunks={}",
                    document.getId(), enrichedChunks.size());
        } catch (Exception exception) {
            log.error("Document processing failed. documentId={}", job.getDocumentId(), exception);

            job.setStatus(ProcessingStatus.FAILED);
            job.setCompletedAt(Instant.now());
            job.setErrorCode(ProcessingErrorCode.UNKNOWN.name());
            job.setErrorMessage(exception.getMessage());

            documentProcessingJobRepository.save(job);

            document.setStatus(DocumentStatus.FAILED);
            document.setFailureReason(exception.getMessage());

            documentRepository.save(document);
        }
    }

    private void addSourceMetadata(Map<String, Object> metadata, org.springframework.ai.document.Document chunk) {
        Map<String, Object> chunkMetadata = chunk.getMetadata();

        putIfPresent(metadata, "page_number", chunkMetadata.get("page_number"));
        putIfPresent(metadata, "section_name", chunkMetadata.get("section_name"));
        putIfPresent(metadata, "sheet_name", chunkMetadata.get("sheet_name"));
        putIfPresent(metadata, "slide_number", chunkMetadata.get("slide_number"));
    }

    private void putIfPresent(Map<String, Object> metadata, String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
    }
}
