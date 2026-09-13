package org.aiknowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.entity.User;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.integration.document.DocumentRetrievalService;
import org.aiknowledge.integration.embedding.EmbeddingService;
import org.aiknowledge.projection.SimilarChunkProjection;
import org.aiknowledge.repository.UserRepository;
import org.aiknowledge.security.SecurityUserService;
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
    private final EmbeddingService embeddingService;
    private final DocumentRetrievalService documentRetrievalService;

    public void processQuestion(UUID documentId, String userQuery) {
        User user = userRepository.findById(userService.getCurrentUserId()).orElseThrow(() -> {
            log.warn("authenticated user could not be resolved");
            return new ResourceNotFoundException("Authenticated user not found");
        });

        if (!documentService.validateAccess(user.getId(), documentId)) {
            log.warn("User does not have access to the requested document");
            throw new ResourceNotFoundException("Document not found");
        }
        userQuery = normalizeQuery(userQuery);

        float[] queryVector = embeddingService.embedQuery(userQuery);

        List<SimilarChunkProjection> vectorChunks = documentRetrievalService.retrieve(documentId, queryVector);

        vectorChunks.stream().forEach(System.out::println);
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        return query.trim().replaceAll("\\s+", " ");
    }
}
