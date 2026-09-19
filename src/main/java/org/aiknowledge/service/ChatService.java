package org.aiknowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.dto.GuardrailResult;
import org.aiknowledge.dto.request.ChatQuestionRequest;
import org.aiknowledge.entity.Message;
import org.aiknowledge.entity.User;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.integration.guardrails.ChatGuardrailService;
import org.aiknowledge.integration.prompts.ChatPromptBuilder;
import org.aiknowledge.integration.rag.DocumentContextBuilder;
import org.aiknowledge.integration.rag.DocumentRerankingService;
import org.aiknowledge.integration.rag.DocumentRetrievalService;
import org.aiknowledge.integration.rag.model.RagContext;
import org.aiknowledge.repository.UserRepository;
import org.aiknowledge.websocket.dto.ConversationResult;
import org.aiknowledge.websocket.dto.PreparedChat;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final UserRepository userRepository;
    private final DocumentService documentService;
    private final DocumentRetrievalService documentRetrievalService;
    private final DocumentRerankingService documentRerankingService;
    private final DocumentContextBuilder contextBuilder;
    private final ConversationService conversationService;
    private final ChatModel chatModel;
    private final ChatPromptBuilder chatPromptBuilder;
    private final ChatGuardrailService chatGuardrailService;

    public PreparedChat prepareChat(ChatQuestionRequest questionRequest) {
        User user = userRepository.findById(questionRequest.userId()).orElseThrow(() -> {
            log.warn("Authenticated user could not be resolved");
            return new ResourceNotFoundException("Authenticated user not found");
        });

        if (!documentService.validateAccess(user.getId(), questionRequest.documentId())) {
            log.warn("User does not have access to the requested document. documentId={}", questionRequest.documentId());
            throw new ResourceNotFoundException("Document not found");
        }

        String userQuery = normalizeQuery(questionRequest.userQuery());

        GuardrailResult guardrailResult = chatGuardrailService.validateInput(userQuery);

        if (!guardrailResult.allowed()) {
            log.warn("Input rejected by guardrail.");
            return new PreparedChat(null, null, false, null, List.of(),
                    guardrailResult.message());
        }

        ConversationResult conversationResult = conversationService.getOrCreateConversation(user.getId(),
                questionRequest.documentId(), questionRequest.conversationId());

        conversationService.saveUserMessage(conversationResult.conversationId(), userQuery);

        ChatQuestionRequest request = ChatQuestionRequest.builder()
                .documentId(questionRequest.documentId())
                .userId(questionRequest.userId())
                .conversationId(conversationResult.conversationId())
                .userQuery(userQuery)
                .build();

        List<Document> documentList = documentRetrievalService.retrieve(request);

        List<Document> rerankedDocuments = documentRerankingService.rerank(userQuery, documentList);

        List<Message> conversationHistory = conversationService.getRecentHistory(conversationResult.conversationId());

        RagContext ragContext = new RagContext(userQuery, conversationHistory, rerankedDocuments);

        String context = contextBuilder.build(ragContext);

        Prompt prompt = chatPromptBuilder.chatPrompt(context, userQuery);

        return new PreparedChat(conversationResult.conversationId(), conversationResult.title(),
                conversationResult.newlyCreated(), prompt, rerankedDocuments, null);
    }

    public Flux<String> generateStream(Prompt prompt) {
        return chatModel.stream(prompt)
                .map(chatResponse -> chatResponse.getResult().getOutput().getText())
                .filter(chunk -> !chunk.isEmpty());
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        return query.trim().replaceAll("\\s+", " ");
    }
}
