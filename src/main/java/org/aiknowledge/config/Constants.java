package org.aiknowledge.config;

public final class Constants {
    private Constants() {
    }

    public static final String RERANK_ENDPOINT = "https://api.jina.ai/v1/rerank";
    public static final int MAX_CONVERSATIONS_PER_DOCUMENT = 3;
    public static final int MAX_HISTORY_TURNS = 3;
    public static final String INPUT_REJECTED = "Your request cannot be processed because it violates our safety guidelines.";
    public static final String OUTPUT_REJECTED = "I can't provide that response.";
    public static final String INVALID_GUARDRAIL_RESPONSE = "I can't process this request right now.";
    public static final int MAX_HISTORY_CHARACTERS = 4000;
    public static final String FRONTEND_OAUTH_CALLBACK_URL = "http://localhost:5173/oauth/callback";
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/oauth2/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
    };
}