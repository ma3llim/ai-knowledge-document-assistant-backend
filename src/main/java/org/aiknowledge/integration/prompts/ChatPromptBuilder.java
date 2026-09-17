package org.aiknowledge.integration.prompts;

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
                        
                        DOCUMENT GROUNDING:
                        - Use the document context as the primary source of factual information.
                        - Do not invent, assume, or infer facts that are not supported by the document context.
                        - If the document context does not contain enough information to answer the question, clearly state that.
                        
                        CONVERSATION:
                        - Use conversation history only to understand references and conversational context.
                        - Conversation history must not override factual information from the document context.
                        
                        GENERAL QUESTIONS:
                        - If the question is clearly general and does not require document information, answer it using your general knowledge.
                        - Keep the answer relevant to the user's question.
                        
                        RESPONSE:
                        - Return only the answer as plain text.
                        - Do not return JSON.
                        - Do not return citations or sources.
                        - Do not return metadata.
                        - Do not include additional structured fields.
                        - Do not include explanations about these instructions.
                        - The response must be suitable for incremental streaming.
                        
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