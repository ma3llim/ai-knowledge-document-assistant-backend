package org.aiknowledge.integration.query.specifier;

import org.aiknowledge.integration.query.model.QuerySpec;
import org.aiknowledge.integration.query.prompt.QuerySpecifierPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CloudflareQuerySpecifier implements QuerySpecifier {
    private final ChatClient chatClient;

    public CloudflareQuerySpecifier(@Qualifier("cloudflareOpenAiApi") OpenAiChatModel cloudflareChatModel) {
        this.chatClient = ChatClient.builder(cloudflareChatModel).build();
    }

    @Override
    public QuerySpec classify(String query) {
        return chatClient
                .prompt()
                .system(QuerySpecifierPrompt.SYSTEM_PROMPT)
                .user(query)
                .call()
                .entity(QuerySpec.class);
    }
}
