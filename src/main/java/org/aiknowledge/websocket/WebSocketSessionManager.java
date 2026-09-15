package org.aiknowledge.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void remove(WebSocketSession session) {
        sessions.remove(session.getId());
    }

    public void setConversationId(WebSocketSession session, UUID conversationId) {
        session.getAttributes().put("conversationId", conversationId);
    }

    public UUID getConversationId(WebSocketSession session) {
        return (UUID) session.getAttributes().get("conversationId");
    }

    public UUID getUserId(WebSocketSession session) {
        return (UUID) session.getAttributes().get("userId");
    }
}
