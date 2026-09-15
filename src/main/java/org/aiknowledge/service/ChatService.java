package org.aiknowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.dto.request.ChatQuestionRequest;
import org.aiknowledge.entity.Message;
import org.aiknowledge.entity.User;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.integration.rag.DocumentContextBuilder;
import org.aiknowledge.integration.rag.DocumentRerankingService;
import org.aiknowledge.integration.rag.DocumentRetrievalService;
import org.aiknowledge.repository.UserRepository;
import org.aiknowledge.security.SecurityUserService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final UserRepository userRepository;
    private final SecurityUserService userService;
    private final DocumentService documentService;
    private final DocumentRetrievalService documentRetrievalService;
    private final DocumentRerankingService documentRerankingService;
    private final DocumentContextBuilder contextBuilder;
    private final ConversationService conversationService;
    private final ChatClient chatClient;

    public void processQuestion(ChatQuestionRequest questionRequest) {
        User user = userRepository.findById(userService.getCurrentUserId()).orElseThrow(() -> {
            log.warn("Authenticated user could not be resolved");
            return new ResourceNotFoundException("Authenticated user not found");
        });

        if (!documentService.validateAccess(user.getId(), questionRequest.documentId())) {
            log.warn("User does not have access to the requested document. documentId={}", questionRequest.documentId());
            throw new ResourceNotFoundException("Document not found");
        }

        String userQuery = normalizeQuery(questionRequest.userQuery());

        UUID conversationId = conversationService.getOrCreateConversation(user.getId(), questionRequest.documentId(), questionRequest.conversationId());

        conversationService.saveUserMessage(conversationId, userQuery);

        ChatQuestionRequest request = ChatQuestionRequest.builder()
                .documentId(questionRequest.documentId())
                .userId(userService.getCurrentUserId())
                .conversationId(conversationId)
                .userQuery(userQuery)
                .build();

        // Top 5
        List<Document> documentList = documentRetrievalService.retrieve(request);
        // Top 3
        List<Document> rerankedDocuments = documentRerankingService.rerank(userQuery, documentList);
        // Previous 3 conversation turns
        List<Message> conversationHistory = conversationService.getRecentHistory(conversationId);
        // Documents + history
        String context = contextBuilder.build(rerankedDocuments, conversationHistory);

    }

    public String generate(List<org.springframework.ai.chat.messages.Message> messages) {
        ChatResponse response = chatClient
                .prompt()
                .messages(messages)
                .call()
                .chatResponse();

        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            throw new IllegalStateException("LLM returned an empty response");
        }

        return response.getResult()
                .getOutput()
                .getText();
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        return query.trim().replaceAll("\\s+", " ");
    }
}
