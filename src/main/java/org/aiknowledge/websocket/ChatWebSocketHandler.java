package org.aiknowledge.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.dto.request.ChatQuestionRequest;
import org.aiknowledge.dto.response.ChatStartData;
import org.aiknowledge.enums.ChatWebSocketEventType;
import org.aiknowledge.service.ChatService;
import org.aiknowledge.service.chat.ConversationService;
import org.aiknowledge.validation.ChatResponseValidator;
import org.aiknowledge.websocket.dto.ChatWebSocketError;
import org.aiknowledge.websocket.dto.ChatWebSocketEvent;
import org.aiknowledge.websocket.dto.ChatWebSocketRequest;
import org.aiknowledge.websocket.dto.PreparedChat;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.Disposable;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final ChatResponseValidator chatResponseValidator;
    private final ConversationService conversationService;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ChatWebSocketRequest request = objectMapper.readValue(message.getPayload(), ChatWebSocketRequest.class);

            if (request.type() == ChatWebSocketEventType.CANCEL) {
                cancelCurrentStream(session);
                return;
            }

            if (request.type() == ChatWebSocketEventType.DISCONNECT) {
                disconnect(session);
                return;
            }

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

            PreparedChat preparedChat = chatService.prepareChat(chatQuestionRequest);

            if (preparedChat.guardrailMessage() != null) {
                sendError(session, "GUARDRAIL_REJECTED", preparedChat.guardrailMessage());
                return;
            }

            ChatStartData startData = objectMapper.convertValue(preparedChat, ChatStartData.class);

            StringBuilder answerBuffer = new StringBuilder();

            Disposable subscription = chatService.processQuestion(chatQuestionRequest)
                    .doOnSubscribe(subscription1 ->
                            sendEvent(session, new ChatWebSocketEvent(ChatWebSocketEventType.START, startData)))
                    .doOnNext(chunk -> {
                                if (!chunk.isEmpty()) {
                                    answerBuffer.append(chunk);

                                    sendEvent(session, new ChatWebSocketEvent(ChatWebSocketEventType.CONTENT, chunk));
                                }
                            }
                    )
                    .doOnComplete(() -> {
                        String finalAnswer = answerBuffer.toString();

                        try {
                            String validateFinalAnswer = chatResponseValidator.validate(finalAnswer);

                            conversationService.saveAssistantMessage(preparedChat.conversationId(), validateFinalAnswer);

                            sendEvent(session, new ChatWebSocketEvent(ChatWebSocketEventType.COMPLETE, null));
                        } catch (Exception exception) {
                            log.error("Failed to finalize chat response", exception);
                        }
                    })
                    .doOnError(exception -> {
                        log.error("LLM streaming failed", exception);
                        sendError(session, "INTERNAL_ERROR", "Unable to process the request.");
                    })
                    .subscribe();

            session.getAttributes().put("streamSubscription", subscription);
        } catch (Exception exception) {
            log.error("Failed to process WebSocket message. sessionId={}", session.getId(), exception);

            sendError(session, "INVALID_REQUEST", "Invalid WebSocket request.");
        }
    }

    private void cancelCurrentStream(WebSocketSession session) {
        Disposable subscription = (Disposable) session.getAttributes().remove("streamSubscription");

        if (subscription != null && !subscription.isDisposed()) {
            log.info("Cancelling active chat stream. sessionId={}", session.getId());
            subscription.dispose();
        }
    }

    private void disconnect(WebSocketSession session) {
        log.info("Client requested WebSocket disconnect. sessionId={}", session.getId());

        cancelCurrentStream(session);

        sessionManager.remove(session);

        try {
            if (session.isOpen()) {
                session.close(CloseStatus.NORMAL);
            }
        } catch (Exception exception) {
            log.error("Failed to close WebSocket session. sessionId={}", session.getId(), exception);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionManager.register(session);
        log.info("WebSocket connection established. sessionId={}, userId={}", session.getId(),
                sessionManager.getUserId(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Disposable subscription = (Disposable) session.getAttributes().remove("streamSubscription");

        if (subscription != null) {
            subscription.dispose();
        }

        sessionManager.remove(session);

        log.info("WebSocket connection closed. sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
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
        sendEvent(session, new ChatWebSocketEvent(ChatWebSocketEventType.ERROR, new ChatWebSocketError(code, message)));
    }
}
