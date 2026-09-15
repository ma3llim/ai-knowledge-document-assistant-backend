package org.aiknowledge.integration.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.config.Constants;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JinaRerankingClient {
    private final AppProperties properties;

    public List<Document> rerank(String userQuery, List<Document> documents) {

        List<String> documentTexts = documents.stream().map(Document::getText).toList();

        JinaRerankRequest request = new JinaRerankRequest(
                properties.ai().reranking().jina().model(),
                userQuery,
                properties.ai().reranking().topK(),
                documentTexts,
                false
        );

        JinaRerankResponse response = RestClient.builder()
                .baseUrl(Constants.RERANK_ENDPOINT)
                .defaultHeader("Authorization", "Bearer " + properties.ai().reranking().jina().apiKey())
                .defaultHeader("Content-Type", "application/json")
                .build()
                .post()
                .body(request)
                .retrieve()
                .body(JinaRerankResponse.class);

        if (response == null || response.results() == null) {
            log.warn("Jina reranker returned empty response");
            return List.of();
        }

        return response.results().stream().map(result -> documents.get(result.index())).toList();
    }

    public record JinaRerankRequest(
            String model,
            String query,
            int top_n,
            List<String> documents,
            boolean return_documents
    ) {
    }

    public record JinaRerankResponse(List<JinaRerankResult> results) {
    }

    public record JinaRerankResult(int index, double relevance_score) {
    }
}