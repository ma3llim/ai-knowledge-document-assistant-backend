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
import org.aiknowledge.integration.rag.model.RagContext;
import org.aiknowledge.repository.UserRepository;
import org.aiknowledge.security.SecurityUserService;
import org.aiknowledge.service.chat.ChatPromptBuilder;
import org.aiknowledge.service.chat.ConversationService;
import org.aiknowledge.websocket.dto.PreparedChat;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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

    public Flux<String> processQuestion(ChatQuestionRequest questionRequest) {
        PreparedChat preparedChat = prepareChat(questionRequest);

        return generateStream(preparedChat.prompt());
    }

    private PreparedChat prepareChat(ChatQuestionRequest questionRequest) {
        User user = userRepository.findById(userService.getCurrentUserId()).orElseThrow(() -> {
            log.warn("Authenticated user could not be resolved");
            return new ResourceNotFoundException("Authenticated user not found");
        });

        if (!documentService.validateAccess(user.getId(), questionRequest.documentId())) {
            log.warn("User does not have access to the requested document. documentId={}", questionRequest.documentId());
            throw new ResourceNotFoundException("Document not found");
        }

        String userQuery = normalizeQuery(questionRequest.userQuery());

        UUID conversationId = conversationService.getOrCreateConversation(user.getId(),
                questionRequest.documentId(), questionRequest.conversationId());

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

        return new PreparedChat(conversationId, prompt, rerankedDocuments);

//        ChatResponseDto validatedResponse = chatResponseValidator.validate(llmResponse);
//        List<Document> citedDocuments = citationService.resolve(validatedResponse.citations(), rerankedDocuments);
//
//        Message assistantMessage = conversationService.saveAssistantMessage(conversationId, validatedResponse.answer());
//        citationService.saveCitations(assistantMessage.getId(), citedDocuments);
//
//        List<CitationResponse> citationResponses = citationService.toCitationResponses(citedDocuments);
//
////            return new ChatApiResponse(validatedResponse.answer(), citationResponses);
//        return null;
    }

    public Flux<String> generateStream(Prompt prompt) {
        return chatClient
                .prompt(prompt)
                .stream()
                .content()
                .doOnNext(chunk -> log.debug("LLM stream chunk received: {}", chunk))
                .doOnError(exception -> log.error("LLM streaming failed", exception));
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        return query.trim().replaceAll("\\s+", " ");
    }
}
