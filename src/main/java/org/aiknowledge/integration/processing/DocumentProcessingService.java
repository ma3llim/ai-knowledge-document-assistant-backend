package org.aiknowledge.integration.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.entity.Document;
import org.aiknowledge.entity.DocumentChunk;
import org.aiknowledge.entity.DocumentProcessingJob;
import org.aiknowledge.enums.DocumentStatus;
import org.aiknowledge.enums.ProcessingErrorCode;
import org.aiknowledge.enums.ProcessingStatus;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.integration.document.DocumentContentNormalizer;
import org.aiknowledge.integration.document.DocumentReaderFactory;
import org.aiknowledge.integration.embedding.EmbeddingService;
import org.aiknowledge.integration.sqs.publisher.DocumentSummaryPublisher;
import org.aiknowledge.integration.storage.ObjectStorageService;
import org.aiknowledge.integration.summarization.ChunkSummarizer;
import org.aiknowledge.repository.DocumentChunkRepository;
import org.aiknowledge.repository.DocumentProcessingJobRepository;
import org.aiknowledge.repository.DocumentRepository;
import org.aiknowledge.repository.SummaryProcessingJobRepository;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentProcessingService {
    private final DocumentRepository documentRepository;
    private final ObjectStorageService objectStorageService;
    private final DocumentReaderFactory documentReaderFactory;
    private final TokenTextSplitter tokenTextSplitter;
    private final DocumentContentNormalizer documentContentNormalizer;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentProcessingJobRepository documentProcessingJobRepository;
    private final SummaryProcessingJobRepository summaryProcessingJobRepository;
    private final ChunkSummarizer chunkSummarizer;
    private final DocumentSummaryPublisher documentSummaryPublisher;

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

            List<org.springframework.ai.document.Document> chunks = tokenTextSplitter.apply(documents);

            List<float[]> embeddings = embeddingService.embed(chunks);
            log.info("Document embedding completed. documentId={}, embeddings={}", job.getDocumentId(), embeddings.size());

            List<DocumentChunk> documentChunks = new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk documentChunk = buildDocumentChunk(job.getDocumentId(), i, chunks.get(i), embeddings.get(i));
                documentChunks.add(documentChunk);
            }
            documentChunkRepository.saveAll(documentChunks);

            document.setStatus(DocumentStatus.READY);
            document.setProcessedAt(Instant.now());
            documentRepository.save(document);

            job.setStatus(ProcessingStatus.COMPLETED);
            job.setCompletedAt(Instant.now());
            documentProcessingJobRepository.save(job);

//            SummaryProcessingJob summaryProcessingJob = SummaryProcessingJob.builder()
//                    .documentId(document.getId())
//                    .status(DocumentStatus.PROCESSING)
//                    .build();
//
//            summaryProcessingJobRepository.save(summaryProcessingJob);
            log.info("Document chunks saved successfully. documentId={}, chunks={}", job.getDocumentId(), documentChunks.size());

            // documentSummaryPublisher.publish(new DocumentSummaryEvent(summaryProcessingJob.getId()));
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

    private DocumentChunk buildDocumentChunk(
            UUID documentId, int chunkIndex, org.springframework.ai.document.Document chunk, float[] embedding) {
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
