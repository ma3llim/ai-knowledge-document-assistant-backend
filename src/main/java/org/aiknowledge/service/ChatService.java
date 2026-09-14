package org.aiknowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.entity.User;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.integration.document.DocumentRetrievalService;
import org.aiknowledge.integration.embedding.EmbeddingService;
import org.aiknowledge.integration.query.model.QuerySpec;
import org.aiknowledge.integration.query.specifier.QuerySpecifier;
import org.aiknowledge.projection.SimilarChunkProjection;
import org.aiknowledge.repository.UserRepository;
import org.aiknowledge.security.SecurityUserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final UserRepository userRepository;
    private final SecurityUserService userService;
    private final DocumentService documentService;
    private final QuerySpecifier querySpecifier;
    private final EmbeddingService embeddingService;
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

        QuerySpec querySpec = querySpecifier.classify(userQuery);

        List<SimilarChunkProjection> vectorChunks = new ArrayList<>();

        switch (querySpec.retrievalStrategy()) {
            case NONE -> log.info("No document retrieval required.");
            case SEMANTIC_SEARCH -> {
                float[] queryVector = embeddingService.embedQuery(userQuery);
                vectorChunks = documentRetrievalService.retrieve(documentId, queryVector);
            }
        }


    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        return query.trim().replaceAll("\\s+", " ");
    }
}
