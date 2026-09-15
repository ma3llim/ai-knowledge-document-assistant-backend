package org.aiknowledge.service.chat;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ChatPromptBuilder {
    public Prompt chatPrompt(String conversationHistory, String documentContext, String userQuery) {
        PromptTemplate promptTemplate = PromptTemplate.builder().template("""
                        You are an AI knowledge assistant.
                        Answer the user's question using the provided document context and conversation history.
                        
                        Rules:
                        - Use the document context as the primary source of information.
                        - Use conversation history only to understand conversational context.
                        - Do not invent information that is not supported by the document context.
                        - If the answer is not available in the document context, clearly say so.
                        - Be concise and directly answer the user's question.
                        
                        CONVERSATION HISTORY:
                        {conversation_history}
                        
                        DOCUMENT CONTEXT:
                        {document_context}
                        
                        CURRENT USER QUESTION:
                        {user_query}
                        """)
                .build();
        
        return promptTemplate.create(Map.of(
                "conversation_history", conversationHistory,
                "document_context", documentContext,
                "user_query", userQuery
        ));
    }
}
