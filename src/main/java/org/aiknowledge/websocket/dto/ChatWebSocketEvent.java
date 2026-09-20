package org.aiknowledge.websocket.dto;

import org.aiknowledge.enums.ChatWebSocketEventType;

public record ChatWebSocketEvent(
        ChatWebSocketEventType type,
        Object data
) {
}
