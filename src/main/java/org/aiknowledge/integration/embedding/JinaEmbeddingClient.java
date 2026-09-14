package org.aiknowledge.integration.embedding;

import org.aiknowledge.config.AppProperties;
import org.aiknowledge.dto.request.JinaEmbeddingRequest;
import org.aiknowledge.dto.response.JinaEmbeddingResponse;
import org.aiknowledge.exception.JinaRateLimitException;
import org.aiknowledge.exception.JinaTransientException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;


@Component
public class JinaEmbeddingClient {
    private final AppProperties properties;
    private final RestClient restClient;

    public JinaEmbeddingClient(AppProperties appProperties) {
        this.properties = appProperties;
        this.restClient = RestClient.builder().baseUrl(properties.ai().embedding().jina().baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.ai().embedding().jina().apiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Retryable(
            retryFor = {
                    JinaRateLimitException.class,
                    JinaTransientException.class
            },
            backoff = @Backoff(
                    delay = 1000,
                    multiplier = 2.0,
                    maxDelay = 10000
            )
    )
    public JinaEmbeddingResponse embed(List<String> texts) {
        JinaEmbeddingRequest request = new JinaEmbeddingRequest(properties.ai().embedding().jina().model(), "retrieval.passage", texts, 1024);

        try {
            return restClient.post()
                    .uri("/v1/embeddings")
                    .body(request)
                    .retrieve()
                    .body(JinaEmbeddingResponse.class);
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();

            if (status == 429) {
                throw new JinaRateLimitException("Jina API rate limit exceeded", exception);
            }
            if (status >= 500 && status <= 599) {
                throw new JinaTransientException("Jina API temporary server error: " + status, exception);
            }
            throw exception;
        } catch (RestClientException exception) {
            throw new JinaTransientException("Temporary Jina API connection failure", exception);
        }
    }
}
