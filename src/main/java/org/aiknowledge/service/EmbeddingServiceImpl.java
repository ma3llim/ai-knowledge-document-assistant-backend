package org.aiknowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {
    private final EmbeddingModel embeddingModel;

    @Override
    public List<float[]> embed(List<Document> documents) {
        List<String> texts = documents.stream().map(Document::getText).toList();

        List<float[]> embeddings = embeddingModel.embed(texts);

        log.info("Embeddings generated successfully. documents={}, embeddings={}", documents.size(), embeddings.size());

        return embeddings;
    }
}
