package org.aiknowledge.integration.ranking;

import java.util.List;

public class JinaRerankingClient {

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