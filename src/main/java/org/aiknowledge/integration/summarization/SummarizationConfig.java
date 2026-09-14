package org.aiknowledge.integration.summarization;

import org.aiknowledge.config.AppProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SummarizationConfig {
    @Bean
    public OpenAiChatModel summarizationModel(AppProperties properties) {
        var api = OpenAiApi.builder()
                .apiKey(properties.ai().summarization().apiKey())
                .baseUrl(properties.ai().summarization().baseUrl())
                .build();

        var options = OpenAiChatOptions.builder()
                .model(properties.ai().summarization().model())
                .temperature(properties.ai().summarization().temperature())
                .maxTokens(properties.ai().summarization().maxTokens())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .build();
    }
}
