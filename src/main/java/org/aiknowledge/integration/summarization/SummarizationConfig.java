package org.aiknowledge.integration.summarization;

import org.aiknowledge.properties.SummarizationProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SummarizationConfig {
    @Bean
    public OpenAiChatModel summarizationModel(SummarizationProperties properties) {
        var api = OpenAiApi.builder()
                .apiKey(properties.apiKey())
                .baseUrl(properties.baseUrl())
                .build();

        var options = OpenAiChatOptions.builder()
                .model(properties.model())
                .temperature(properties.temperature())
                .maxTokens(properties.maxTokens())
                .build();
        
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .build();
    }
}
