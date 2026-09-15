package org.aiknowledge.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final WebSocketSessionManager sessionManager;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UUID userId =
                (UUID) session.getAttributes().get("userId");

        log.info(
                "WebSocket message received. sessionId={}, userId={}, payload={}",
                session.getId(),
                userId,
                message.getPayload()
        );
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionManager.register(session);
        log.info(
                "WebSocket connection established. sessionId={}, userId={}",
                session.getId(),
                sessionManager.getUserId(session)
        );
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionManager.remove(session);
        log.info("WebSocket connection closed. sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error. sessionId={}", session.getId(), exception);
        sessionManager.remove(session);
    }
}
