package org.aiknowledge.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.config.Constants;
import org.aiknowledge.config.ratelimit.RateLimitKeyResolver;
import org.aiknowledge.config.ratelimit.RedisRateLimitService;
import org.aiknowledge.dto.request.ChatQuestionRequest;
import org.aiknowledge.dto.response.ChatStartData;
import org.aiknowledge.enums.ChatWebSocketEventType;
import org.aiknowledge.service.ChatService;
import org.aiknowledge.service.ConversationService;
import org.aiknowledge.validation.ChatResponseValidator;
import org.aiknowledge.websocket.dto.ChatWebSocketError;
import org.aiknowledge.websocket.dto.ChatWebSocketEvent;
import org.aiknowledge.websocket.dto.ChatWebSocketRequest;
import org.aiknowledge.websocket.dto.PreparedChat;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.Disposable;

import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final ChatResponseValidator chatResponseValidator;
    private final ConversationService conversationService;
    private final ThreadPoolTaskScheduler heartbeatScheduler;
    private final AppProperties properties;
    private final RedisRateLimitService rateLimitService;
    private final RateLimitKeyResolver keyResolver;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        AppProperties.RateLimit rateLimit = properties.rateLimit();

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

            if (rateLimit.websocket().enabled()) {
                String rateLimitKey = keyResolver.resolveWebSocketMessageKey(request.userId().toString());

                var result = rateLimitService.check(rateLimitKey, rateLimit.llm().requestsPerWindow(),
                        rateLimit.llm().windowSeconds());

                if (!result.allowed()) {
                    log.warn("WebSocket message rate limit exceeded. userId={}, sessionId={}, retryAfterSeconds={}",
                            request.userId(), session.getId(), result.retryAfterSeconds());

                    sendRateLimitError(session, result.retryAfterSeconds());
                    return;
                }
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

            Disposable subscription = chatService.generateStream(preparedChat.prompt())
                    .doOnSubscribe(subscription1 -> {
                        session.getAttributes().put(Constants.STREAM_SUBSCRIPTION, subscription1);
                        sendEvent(session, new ChatWebSocketEvent(ChatWebSocketEventType.START, startData));
                    })
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
                    .doFinally(signalType -> session.getAttributes().remove(Constants.STREAM_SUBSCRIPTION))
                    .subscribe();
        } catch (Exception exception) {
            log.error("Failed to process WebSocket message. sessionId={}", session.getId(), exception);

            sendError(session, "INVALID_REQUEST", "Invalid WebSocket request.");
        }
    }

    private void cancelCurrentStream(WebSocketSession session) {
        Disposable subscription = (Disposable) session.getAttributes().remove(Constants.STREAM_SUBSCRIPTION);

        if (subscription != null && !subscription.isDisposed()) {
            log.info("Cancelling active chat stream. sessionId={}", session.getId());
            subscription.dispose();
        }
    }

    private void disconnect(WebSocketSession session) {
        log.info("Client requested WebSocket disconnect. sessionId={}", session.getId());

        cancelCurrentStream(session);
        cleanupHeartbeat(session);

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

        session.getAttributes().put(Constants.LAST_PONG, System.nanoTime());

        ScheduledFuture<?> heartbeatTask = heartbeatScheduler.scheduleAtFixedRate(
                () -> sendHeartbeat(session), Constants.HEARTBEAT_INTERVAL);

        session.getAttributes().put(Constants.HEARTBEAT_TASK, heartbeatTask);

        log.info("WebSocket connection established. sessionId={}, userId={}", session.getId(),
                sessionManager.getUserId(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cleanupHeartbeat(session);

        Disposable subscription = (Disposable) session.getAttributes().remove(Constants.STREAM_SUBSCRIPTION);

        if (subscription != null) {
            subscription.dispose();
        }

        sessionManager.remove(session);

        log.info("WebSocket connection closed. sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error. sessionId={}", session.getId(), exception);

        cleanupHeartbeat(session);

        Disposable subscription = (Disposable) session.getAttributes().remove(Constants.STREAM_SUBSCRIPTION);

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

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        session.getAttributes().put(Constants.LAST_PONG, System.nanoTime());
        log.debug("WebSocket pong received. sessionId={}", session.getId());
    }

    private void sendHeartbeat(WebSocketSession session) {
        if (!session.isOpen()) {
            cleanupHeartbeat(session);
            return;
        }

        Long lastPong = (Long) session.getAttributes().get(Constants.LAST_PONG);

        if (lastPong != null) {
            long elapsedNanos = System.nanoTime() - lastPong;
            if (elapsedNanos > Constants.PONG_TIMEOUT.toNanos()) {
                log.warn("WebSocket heartbeat timeout. Closing session. sessionId={}", session.getId());

                try {
                    session.close(CloseStatus.GOING_AWAY);
                } catch (Exception exception) {
                    log.error("Failed to close heartbeat-timeout session. sessionId={}", session.getId(), exception);
                }

                cleanupHeartbeat(session);
                sessionManager.remove(session);
                return;
            }
        }
        try {
            synchronized (session) {
                session.sendMessage(new PingMessage(ByteBuffer.allocate(0)));
            }

            log.debug("WebSocket ping sent. sessionId={}", session.getId());
        } catch (Exception exception) {
            log.warn("Failed to send WebSocket ping. sessionId={}", session.getId(), exception);

            cleanupHeartbeat(session);
            sessionManager.remove(session);
        }
    }

    private void cleanupHeartbeat(WebSocketSession session) {
        ScheduledFuture<?> heartbeatTask = (ScheduledFuture<?>) session.getAttributes().remove(Constants.HEARTBEAT_TASK);

        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
        }

        session.getAttributes().remove(Constants.LAST_PONG);
    }

    private void sendRateLimitError(WebSocketSession session, long retryAfterSeconds) {
        try {
            ChatWebSocketError error = new ChatWebSocketError("RATE_LIMIT_EXCEEDED",
                    "Too many messages. Please try again later.");

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (Exception exception) {
            log.error("Failed to send WebSocket rate-limit error. sessionId={}", session.getId(), exception);
        }
    }
}
