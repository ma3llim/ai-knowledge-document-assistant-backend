package org.aiknowledge.websocket.dto;

import java.util.UUID;

public record ConversationResult(
        UUID conversationId,
        String title,
        boolean newlyCreated
) {
}