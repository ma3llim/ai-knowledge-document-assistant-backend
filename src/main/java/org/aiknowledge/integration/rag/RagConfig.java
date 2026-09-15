package org.aiknowledge.integration.rag;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.integration.rag.prompts.DefaultPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RagConfig {
    private final OpenAiChatModel chatModel;

    @Bean
    public ChatClient ragChatClient() {
        return ChatClient.builder(chatModel)
                .defaultSystem(DefaultPrompt.DEFAULT_SYSTEM_PROMPT)
                .build();
    }
}
