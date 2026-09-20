package org.aiknowledge.integration.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.integration.ranking.JinaRerankingClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentRerankingService {
    private final JinaRerankingClient rerankingClient;
    private final AppProperties properties;

    public List<Document> rerank(String userQuery, List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.debug("No documents available for reranking.");
            return Collections.emptyList();
        }

        int topK = properties.ai().reranking().topK();

        if (documents.size() <= topK) {
            log.debug("Reranking skipped. documentCount={}, topK={}", documents.size(), topK);
            return documents;
        }

        long startTime = System.nanoTime();

        try {
            log.info("Document reranking started. documentCount={}, topK={}", documents.size(), topK);

            List<Document> rerankedDocuments = rerankingClient.rerank(userQuery, documents);

            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            log.info("Document reranking completed. inputCount={}, resultCount={}, durationMs={}",
                    documents.size(), rerankedDocuments.size(), durationMs);

            return rerankedDocuments;

        } catch (Exception exception) {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            log.error("Document reranking failed. documentCount={}, durationMs={}, errorType={}",
                    documents.size(), durationMs, exception.getClass().getSimpleName(), exception);
            throw exception;
        }
    }
}
