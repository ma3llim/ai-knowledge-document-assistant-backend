package org.aiknowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.dto.request.ChatQuestionRequest;
import org.aiknowledge.entity.User;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.integration.rag.DocumentRetrievalService;
import org.aiknowledge.repository.UserRepository;
import org.aiknowledge.security.SecurityUserService;
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

    public void processQuestion(UUID documentId, String userQuery) {
        User user = userRepository.findById(userService.getCurrentUserId()).orElseThrow(() -> {
            log.warn("Authenticated user could not be resolved");
            return new ResourceNotFoundException("Authenticated user not found");
        });

        if (!documentService.validateAccess(user.getId(), documentId)) {
            log.warn("User does not have access to the requested document. documentId={}", documentId);
            throw new ResourceNotFoundException("Document not found");
        }

        userQuery = normalizeQuery(userQuery);

        ChatQuestionRequest request = ChatQuestionRequest.builder()
                .documentId(documentId)
                .userId(userService.getCurrentUserId())
                .conversationId(null)
                .userQuery(userQuery)
                .build();

        List<Document> documentList = documentRetrievalService.retrieve(request);
        log.info("documentList: {}", documentList);
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        return query.trim().replaceAll("\\s+", " ");
    }
}
