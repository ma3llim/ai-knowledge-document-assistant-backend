package org.aiknowledge.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.dto.request.ChatQuestionRequest;
import org.aiknowledge.enums.ChatWebSocketEventType;
import org.aiknowledge.service.ChatService;
import org.aiknowledge.websocket.dto.ChatWebSocketError;
import org.aiknowledge.websocket.dto.ChatWebSocketEvent;
import org.aiknowledge.websocket.dto.ChatWebSocketRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.Disposable;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final ChatService chatService;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            ChatWebSocketRequest request = objectMapper.readValue(message.getPayload(), ChatWebSocketRequest.class);

            if (request.userId() == null) {
                sendError(session, "UNAUTHORIZED", "WebSocket authentication required.");
                return;
            }

            ChatQuestionRequest chatQuestionRequest = ChatQuestionRequest.builder()
                    .documentId(request.documentId())
                    .conversationId(request.conversationId())
                    .userId(request.userId())
                    .userQuery(request.userQuery())
                    .build();

            sendEvent(session, new ChatWebSocketEvent(ChatWebSocketEventType.START, null));

            Disposable subscription = chatService.processQuestion(chatQuestionRequest)
                    .doOnNext(chunk -> sendEvent(session, new ChatWebSocketEvent(ChatWebSocketEventType.CONTENT, chunk)))
                    .collect(Collectors.joining())
                    .subscribe(
                            finalAnswer -> {
                                log.debug("LLM stream completed. answerLength={}", finalAnswer.length());
                                
                            },
                            error -> {
                                log.error("WebSocket chat processing failed. sessionId={}", session.getId(), error);
                                sendError(session, "INTERNAL_ERROR", "Unable to process the request.");
                            }
                    );

            session.getAttributes().put("streamSubscription", subscription);
        } catch (Exception exception) {
            log.error("Failed to process WebSocket message. sessionId={}", session.getId(), exception);

            sendError(session, "INVALID_REQUEST", "Invalid WebSocket request.");
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionManager.register(session);
        log.info("WebSocket connection established. sessionId={}, userId={}", session.getId(),
                sessionManager.getUserId(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Disposable subscription = (Disposable) session.getAttributes().remove("streamSubscription");

        if (subscription != null) {
            subscription.dispose();
        }

        sessionManager.remove(session);

        log.info("WebSocket connection closed. sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error. sessionId={}", session.getId(), exception);

        Disposable subscription = (Disposable) session.getAttributes().remove("streamSubscription");

        if (subscription != null) {
            subscription.dispose();
        }

        sessionManager.remove(session);
    }

    private void sendEvent(WebSocketSession session, ChatWebSocketEvent eventType) {
        if (!session.isOpen()) {
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(eventType);

            synchronized (session) {
                session.sendMessage(new TextMessage(payload));
            }
        } catch (Exception exception) {
            log.error("Failed to send WebSocket event. sessionId={}", session.getId(), exception);
        }

    }

    private void sendError(WebSocketSession session, String code, String message) {
        sendEvent(session,
                new ChatWebSocketEvent(
                        ChatWebSocketEventType.ERROR,
                        new ChatWebSocketError(code, message)
                )
        );
    }
}
