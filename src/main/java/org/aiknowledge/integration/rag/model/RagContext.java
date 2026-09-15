package org.aiknowledge.integration.rag.model;

import org.aiknowledge.entity.Message;
import org.springframework.ai.document.Document;

import java.util.List;

public record RagContext(
        String userQuery,
        List<Message> conversationHistory,
        List<Document> retrievedDocuments
) {
}