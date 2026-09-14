package org.aiknowledge.config;

public final class Constants {
    private Constants() {
    }

    public static final String FRONTEND_OAUTH_CALLBACK_URL = "http://localhost:5173/oauth/callback";

    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/oauth2/**"
    };
}
