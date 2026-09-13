package org.aiknowledge.integration.document;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.projection.SimilarChunkProjection;
import org.aiknowledge.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentRetrievalService {
    private final DocumentChunkRepository documentChunkRepository;

    public List<SimilarChunkProjection> retrieve(UUID documentId, float[] queryVector) {
        String vector = toVectorString(queryVector);
        return documentChunkRepository.findSimilarChunks(documentId, vector, 5);
    }

    private String toVectorString(float[] vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(",");
            }

            builder.append(vector[i]);
        }

        builder.append("]");

        return builder.toString();
    }
}
