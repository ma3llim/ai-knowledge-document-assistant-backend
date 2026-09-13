package org.aiknowledge.integration.embedding;

import org.springframework.ai.document.Document;

import java.util.List;

public interface EmbeddingService {
    List<float[]> embed(List<Document> documents);

    float[] embedQuery(String query);
}
