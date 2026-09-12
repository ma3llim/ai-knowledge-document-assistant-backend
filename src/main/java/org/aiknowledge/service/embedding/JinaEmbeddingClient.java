package org.aiknowledge.service.embedding;

import org.aiknowledge.config.properties.JinaEmbeddingProperties;
import org.aiknowledge.dto.request.JinaEmbeddingRequest;
import org.aiknowledge.dto.response.JinaEmbeddingResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;


@Component
public class JinaEmbeddingClient {
    private final JinaEmbeddingProperties jinaEmbeddingProperties;
    private final RestClient restClient;

    public JinaEmbeddingClient(JinaEmbeddingProperties jinaEmbeddingProperties) {
        this.jinaEmbeddingProperties = jinaEmbeddingProperties;

        this.restClient = RestClient.builder().baseUrl(jinaEmbeddingProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jinaEmbeddingProperties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public JinaEmbeddingResponse embed(List<String> texts) {
        JinaEmbeddingRequest request = new JinaEmbeddingRequest(jinaEmbeddingProperties.getModel(), "retrieval.passage", texts, 1024);

        return restClient.post()
                .uri("/v1/embeddings")
                .body(request)
                .retrieve()
                .body(JinaEmbeddingResponse.class);
    }
}
