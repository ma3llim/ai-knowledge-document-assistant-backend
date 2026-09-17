package org.aiknowledge.integration;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.integration.embedding.JinaEmbeddingModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class AiConfig {
    private final AppProperties properties;

    @Bean("chatModel")
    public OpenAiChatModel chatModel() {
        AppProperties.Ai.Chat.Cloudflare cloudflare = properties.ai().chat().cloudflare();

        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(cloudflare.apiKey())
                .baseUrl(cloudflare.baseUrl())
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(cloudflare.model())
                .temperature(cloudflare.temperature())
                .maxTokens(cloudflare.maxTokens())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    @Bean("guardrailChatModel")
    public OpenAiChatModel guardrailChatModel() {
        AppProperties.Ai.Guardrail.Cloudflare guardrail = properties.ai().guardrail().cloudflare();

        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(guardrail.apiKey())
                .baseUrl(guardrail.baseUrl())
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(guardrail.model())
                .temperature(0.0)
                .maxTokens(guardrail.maxTokens())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

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
