package org.aiknowledge.service;

import org.springframework.ai.document.Document;

import java.util.List;

public interface EmbeddingService {
    List<float[]> embed(List<Document> documents);
}
