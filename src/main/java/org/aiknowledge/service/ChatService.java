package org.aiknowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.dto.request.ChatQuestionRequest;
import org.aiknowledge.dto.response.ChatApiResponse;
import org.aiknowledge.dto.response.ChatResponseDto;
import org.aiknowledge.dto.response.CitationResponse;
import org.aiknowledge.entity.Message;
import org.aiknowledge.entity.User;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.integration.rag.DocumentContextBuilder;
import org.aiknowledge.integration.rag.DocumentRerankingService;
import org.aiknowledge.integration.rag.DocumentRetrievalService;
import org.aiknowledge.integration.rag.model.RagContext;
import org.aiknowledge.repository.UserRepository;
import org.aiknowledge.security.SecurityUserService;
import org.aiknowledge.service.chat.ChatPromptBuilder;
import org.aiknowledge.service.chat.CitationService;
import org.aiknowledge.service.chat.ConversationService;
import org.aiknowledge.validation.ChatResponseValidator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
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
    private final ChatPromptBuilder chatPromptBuilder;
    private final CitationService citationService;
    private final ChatResponseValidator chatResponseValidator;

    public ChatApiResponse processQuestion(ChatQuestionRequest questionRequest) {
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

        List<Document> documentList = documentRetrievalService.retrieve(request);

        List<Document> rerankedDocuments = documentRerankingService.rerank(userQuery, documentList);

        List<Message> conversationHistory = conversationService.getRecentHistory(conversationId);

        RagContext ragContext = new RagContext(userQuery, conversationHistory, rerankedDocuments);

        String context = contextBuilder.build(ragContext);

        Prompt prompt = chatPromptBuilder.chatPrompt(context, userQuery);

        ChatResponseDto llmResponse = generate(prompt);

        ChatResponseDto validatedResponse = chatResponseValidator.validate(llmResponse);
        List<Document> citedDocuments = citationService.resolve(validatedResponse.citations(), rerankedDocuments);

        Message assistantMessage = conversationService.saveAssistantMessage(conversationId, validatedResponse.answer());
        citationService.saveCitations(assistantMessage.getId(), citedDocuments);

        List<CitationResponse> citationResponses = citationService.toCitationResponses(citedDocuments);

        return new ChatApiResponse(validatedResponse.answer(), citationResponses);
    }

    public ChatResponseDto generate(Prompt prompt) {
        return chatClient
                .prompt(prompt)
                .call()
                .entity(ChatResponseDto.class);
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        return query.trim().replaceAll("\\s+", " ");
    }
}
