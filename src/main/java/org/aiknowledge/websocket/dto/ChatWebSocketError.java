package org.aiknowledge.websocket.dto;

public record ChatWebSocketError(
        String code,
        String message
) {
}
