package org.aiknowledge.integration.llm.groq;

import org.aiknowledge.properties.GroqProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroqProviderConfig {
    @Bean("groqChatClient")
    public ChatClient groqChatClient(GroqProperties groqProperties) {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(groqProperties.getApiKey())
                .baseUrl(groqProperties.getBaseUrl())
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(groqProperties.getModel())
                .temperature(0.0)
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();

        return ChatClient.builder(chatModel).build();
    }
}
