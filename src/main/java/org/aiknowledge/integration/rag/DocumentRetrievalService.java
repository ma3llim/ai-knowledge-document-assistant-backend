package org.aiknowledge.integration.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.dto.request.ChatQuestionRequest;
import org.springframework.ai.chat.client.ChatClient;
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
    private final ChatClient chatClient;

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
        try {
            List<Document> documents = vectorStore.similaritySearch(searchRequest);
            log.info("Retrieved {} documents. userId={}, documentId={}", documents.size(), userId, documentId);
            return documents;
        } catch (Exception exception) {
            log.error("Document retrieval failed. userId={}, documentId={}", userId, documentId, exception);
            return Collections.emptyList();
        }
    }
}
