package org.aiknowledge.websocket.dto;

import org.aiknowledge.enums.ChatWebSocketEventType;

public record ChatWebSocketResponse(
        ChatWebSocketEventType type,
        Object data
) {
}
