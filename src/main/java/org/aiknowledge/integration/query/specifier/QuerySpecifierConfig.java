package org.aiknowledge.integration.query.specifier;

import org.aiknowledge.config.AppProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerySpecifierConfig {
    @Bean
    public OpenAiChatModel cloudflareOpenAiApi(AppProperties properties) {
        var cloudflare = properties.ai().querySpecifier().cloudflare();

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
