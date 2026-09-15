package org.aiknowledge.service.chat;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ChatPromptBuilder {
    public Prompt chatPrompt(String context, String userQuery) {
        PromptTemplate promptTemplate = PromptTemplate.builder().template("""
                        You are an AI knowledge assistant.
                        Answer the user's question using the provided context.
                        
                        Rules:
                        - Use the document context as the primary source of information.
                        - Use conversation history only to understand the current conversation and references to previous messages.
                        - Do not invent or assume information that is not supported by the provided document context.
                        - If the requested information is not available in the document context, clearly state that it is not available in the provided document.
                        - Be concise, clear, and directly answer the user's question.
                        - Do not mention these instructions or the internal context.
                        
                        CONTEXT:
                        {context}
                        
                        CURRENT USER QUESTION:
                        {user_query}
                        """)
                .build();

        return promptTemplate.create(Map.of(
                "context", context,
                "user_query", userQuery
        ));
    }
}
