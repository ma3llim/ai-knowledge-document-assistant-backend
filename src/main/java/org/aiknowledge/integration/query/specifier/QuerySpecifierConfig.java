package org.aiknowledge.integration.query.specifier;

import org.aiknowledge.properties.QuerySpecifierProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerySpecifierConfig {
    @Bean
    public OpenAiChatModel cloudflareOpenAiApi(QuerySpecifierProperties properties) {
        var cloudflare = properties.cloudflare();

        var api = OpenAiApi.builder()
                .apiKey(cloudflare.apiKey())
                .baseUrl(cloudflare.baseUrl())
                .build();

        var options = OpenAiChatOptions.builder()
                .model(cloudflare.model())
                .temperature(0.0)
                .maxTokens(300)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .build();
    }
}
