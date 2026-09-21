package org.aiknowledge.config;

import java.time.Duration;

public final class Constants {
    private Constants() {
    }

    public static final String RERANK_ENDPOINT = "https://api.jina.ai/v1/rerank";
    public static final String INPUT_REJECTED = "Your request cannot be processed because it violates our safety guidelines.";
    public static final String INVALID_GUARDRAIL_RESPONSE = "I can't process this request right now.";
    public static final int MAX_HISTORY_CHARACTERS = 4000;
    public static final String FRONTEND_OAUTH_CALLBACK_URL = "https://ai.api.mohdsameer.info/oauth/callback";
    public static final String STREAM_SUBSCRIPTION = "streamSubscription";
    public static final int MESSAGE_PAGE_SIZE = 20;
    public static final String LAST_ACTIVITY = "lastActivity";
    public static final String IDLE_TIMEOUT_TASK = "idleTimeoutTask";
    public static final Duration WEBSOCKET_IDLE_TIMEOUT = Duration.ofMinutes(2);
    public static final Duration WEBSOCKET_IDLE_CHECK_INTERVAL = Duration.ofSeconds(30);
    public static final String WEBSOCKET_CONNECTED_AT = "websocketConnectedAt";
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/oauth2/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
    };
}