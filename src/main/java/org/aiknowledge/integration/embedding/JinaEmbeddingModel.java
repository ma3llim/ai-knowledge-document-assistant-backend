package org.aiknowledge.integration.embedding;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.web.client.RestClient;

import java.util.List;

public class JinaEmbeddingModel implements EmbeddingModel {
    private final RestClient restClient;
    private final String model;

    public JinaEmbeddingModel(RestClient restClient, String model) {
        this.restClient = restClient;
        this.model = model;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        JinaEmbeddingRequest jinaRequest = new JinaEmbeddingRequest(
                model,
                request.getInstructions()
        );

        JinaEmbeddingResponse response = restClient.post()
                .uri("/v1/embeddings")
                .body(jinaRequest)
                .retrieve()
                .body(JinaEmbeddingResponse.class);

        List<Embedding> embeddings = response.data()
                .stream()
                .map(item -> new Embedding(item.embedding(), item.index()))
                .toList();

        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return new float[0];
    }

    private record JinaEmbeddingRequest(String model, List<String> input) {
    }

    private record JinaEmbeddingResponse(List<JinaEmbeddingData> data) {
    }

    private record JinaEmbeddingData(int index, float[] embedding) {
    }
}
