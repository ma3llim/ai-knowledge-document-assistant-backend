package org.aiknowledge.websocket.dto;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.UUID;

public record PreparedChat(
        UUID conversationId,
        Prompt prompt,
        List<Document> rerankedDocuments
) {
}
