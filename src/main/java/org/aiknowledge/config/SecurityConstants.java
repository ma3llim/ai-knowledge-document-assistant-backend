package org.aiknowledge.config;

public final class SecurityConstants {
    private SecurityConstants() {
    }

    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/oauth2/**"
    };
}
