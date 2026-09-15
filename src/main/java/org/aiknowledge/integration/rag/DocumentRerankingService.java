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
            log.debug("No documents available for reranking. query={}", userQuery);
            return Collections.emptyList();
        }
        if (documents.size() <= properties.ai().reranking().topK()) {
            return documents;
        }
        return rerankingClient.rerank(userQuery, documents);
    }
}
