package org.aiknowledge.integration.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.entity.Document;
import org.aiknowledge.entity.SummaryProcessingJob;
import org.aiknowledge.enums.DocumentStatus;
import org.aiknowledge.exception.InternalServerException;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.integration.embedding.EmbeddingService;
import org.aiknowledge.integration.summarization.ChunkSummarizer;
import org.aiknowledge.projection.DocumentChunkContentProjection;
import org.aiknowledge.repository.DocumentChunkRepository;
import org.aiknowledge.repository.DocumentRepository;
import org.aiknowledge.repository.SummaryProcessingJobRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentSummaryService {
    private final SummaryProcessingJobRepository summaryProcessingJobRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final ChunkSummarizer chunkSummarizer;
    private final EmbeddingService embeddingService;

    public void processSummary(UUID jobId) {
        log.info("Starting document summary processing. summaryJobId={}", jobId);

        SummaryProcessingJob summaryJob = summaryProcessingJobRepository.findById(jobId).orElseThrow(() ->
                new ResourceNotFoundException("Summary processing job not found: " + jobId)
        );

        Document document = documentRepository.findById(summaryJob.getDocumentId()).orElseThrow(() ->
                new ResourceNotFoundException("Document not found: " + summaryJob.getDocumentId())
        );

        try {
            summaryJob.setStatus(DocumentStatus.PROCESSING);
            summaryProcessingJobRepository.save(summaryJob);

            document.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(document);

            List<DocumentChunkContentProjection> chunks = documentChunkRepository.findAllByDocumentIdOrderByChunkIndex(document.getId());

            log.info("Loaded {} chunks for summary processing. documentId={}", chunks.size(), document.getId());

            for (DocumentChunkContentProjection chunk : chunks) {
                log.debug("Generating summary for chunk. documentId={}, chunkIndex={}", document.getId(),
                        chunk.getChunkIndex());

                String summary = chunkSummarizer.summarize(chunk.getContent());
                log.info("Summary: {}", summary);

                float[] summaryEmbedding = embeddingService.embedQuery(summary);

                log.info("summaryEmbedding: {}", summaryEmbedding);

                documentChunkRepository.updateSummary(chunk.getId(), summary, summaryEmbedding);
            }

            summaryJob.setStatus(DocumentStatus.READY);
            summaryJob.setCompletedAt(java.time.Instant.now());
            summaryJob.setErrorCode(null);
            summaryJob.setErrorMessage(null);

            summaryProcessingJobRepository.save(summaryJob);

            document.setStatus(DocumentStatus.READY);
            document.setFailureReason(null);

            documentRepository.save(document);

            log.info("Document summary processing completed successfully. summaryJobId={}, documentId={}, chunkCount={}",
                    jobId, document.getId(), chunks.size());

        } catch (Exception exception) {
            log.error("Document summary processing failed. summaryJobId={}, documentId={}", jobId, document.getId(), exception);

            summaryJob.setStatus(DocumentStatus.FAILED);
            summaryJob.setCompletedAt(java.time.Instant.now());
            summaryJob.setErrorCode("SUMMARY_PROCESSING_FAILED");
            summaryJob.setErrorMessage(exception.getMessage());

            summaryProcessingJobRepository.save(summaryJob);

            document.setStatus(DocumentStatus.FAILED);
            document.setFailureReason("Document summary processing failed");

            documentRepository.save(document);

            throw new InternalServerException();
        }
    }
}
