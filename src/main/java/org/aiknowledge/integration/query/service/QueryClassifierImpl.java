package org.aiknowledge.integration.query.service;

import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.integration.query.model.QuerySpec;
import org.aiknowledge.integration.query.prompt.QuerySpecifierPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QueryClassifierImpl implements QueryClassifier {
    private final ChatClient groqChatClient;

    public QueryClassifierImpl(@Qualifier("groqChatClient") ChatClient groqChatClient) {
        this.groqChatClient = groqChatClient;
    }

    @Override
    public QuerySpec classify(String query) {
        log.info("Classifying query");

        return groqChatClient
                .prompt()
                .system(QuerySpecifierPrompt.SYSTEM_PROMPT)
                .user(query)
                .call()
                .entity(QuerySpec.class);
    }
}
