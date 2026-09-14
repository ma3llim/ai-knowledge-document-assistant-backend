package org.aiknowledge.integration.summarization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChunkSummarizer {
    private final ChatClient chatClient;

    public ChunkSummarizer(@Qualifier("summarizationModel") OpenAiChatModel summarizationModel) {
        this.chatClient = ChatClient.builder(summarizationModel).build();
    }

    public String summarize(String chunk) {
        return chatClient
                .prompt()
                .system(SummarizingPrompt.SYSTEM_PROMPT)
                .user(chunk)
                .call()
                .content();
    }
}