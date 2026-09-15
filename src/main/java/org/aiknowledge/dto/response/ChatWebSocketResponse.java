package org.aiknowledge.dto.response;

import org.aiknowledge.enums.ChatWebSocketEventType;

public record ChatWebSocketResponse(
        ChatWebSocketEventType type,
        Object data
) {
}
