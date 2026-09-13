package org.aiknowledge.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.entity.User;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.integration.document.DocumentRetrievalService;
import org.aiknowledge.integration.embedding.EmbeddingService;
import org.aiknowledge.integration.query.model.QuerySpec;
import org.aiknowledge.integration.query.service.QueryClassifierImpl;
import org.aiknowledge.repository.UserRepository;
import org.aiknowledge.security.SecurityUserService;
import org.aiknowledge.service.DocumentService;
import org.springframework.stereotype.Service;

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
    private final QueryClassifierImpl queryClassifier;

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
        log.info("Processing question for documentId={}", documentId);

        QuerySpec querySpec = queryClassifier.classify(userQuery);
        log.info("Query Spec: {}", querySpec);
        log.info(
                "Query specified. type={}, intent={}, strategy={}, requiresDocuments={}",
                querySpec.type(),
                querySpec.intent(),
                querySpec.retrievalStrategy(),
                querySpec.requiresDocuments()
        );


//        float[] queryVector = embeddingService.embedQuery(userQuery);
//
//        log.info("Query embedding generated successfully. dimension={}", queryVector.length);
//
//        List<SimilarChunkProjection> vectorChunks = documentRetrievalService.retrieve(documentId, queryVector);
//
//        log.info(
//                "Retrieved {} relevant chunks for documentId={}",
//                vectorChunks.size(),
//                documentId
//        );
//
//        vectorChunks.forEach(chunk ->
//                log.info(
//                        "Retrieved chunk: id={}, similarity={}, page={}",
//                        chunk.getId(),
//                        chunk.getSimilarity(),
//                        chunk.getPageNumber()
//                )
//        );
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        return query.trim().replaceAll("\\s+", " ");
    }
}
