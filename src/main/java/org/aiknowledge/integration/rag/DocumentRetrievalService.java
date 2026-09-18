package org.aiknowledge.integration.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.dto.request.ChatQuestionRequest;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentRetrievalService {
    private final VectorStore vectorStore;
    private final AppProperties appProperties;

    public List<Document> retrieve(ChatQuestionRequest request) {
        return retrieveRelevantDocuments(
                request.userQuery(),
                request.documentId(),
                request.userId(),
                appProperties.ai().rag().retrieval().similarityThreshold(),
                appProperties.ai().rag().retrieval().topK()
        );
    }

    private List<Document> retrieveRelevantDocuments(String userQuery, UUID documentId, UUID userId, double similarityThreshold, int topK
    ) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(userQuery)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .filterExpression(new FilterExpressionBuilder().and(
                        new FilterExpressionBuilder().eq("user_id", userId.toString()),
                        new FilterExpressionBuilder().eq("document_id", documentId.toString())).build()
                ).build();

        long startTime = System.nanoTime();

        try {
            log.info("Document retrieval started. userId={}, documentId={}, topK={}, similarityThreshold={}",
                    userId, documentId, topK, similarityThreshold);

            List<Document> documents = vectorStore.similaritySearch(searchRequest);

            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            log.info("Document retrieval completed. userId={}, documentId={}, resultCount={}, durationMs={}",
                    userId, documentId, documents.size(), durationMs);

            return documents;

        } catch (Exception exception) {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            log.error("Document retrieval failed. userId={}, documentId={}, durationMs={}, errorType={}",
                    userId, documentId, durationMs, exception.getClass().getSimpleName(), exception);
            return Collections.emptyList();
        }
    }
}
