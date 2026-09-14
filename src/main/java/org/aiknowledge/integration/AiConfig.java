package org.aiknowledge.integration;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.integration.embedding.JinaEmbeddingModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class AiConfig {
    private final AppProperties properties;

    @Bean
    public EmbeddingModel embeddingModel() {
        AppProperties.Ai.Embedding.Jina jina = properties.ai().embedding().jina();

        RestClient restClient = RestClient.builder()
                .baseUrl(jina.baseUrl())
                .defaultHeader("Authorization", "Bearer " + jina.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();

        return new JinaEmbeddingModel(restClient, jina.model());
    }
}
