package org.aiknowledge.config.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class RateLimitKeyResolver {
    public String resolveApiKey(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");

        if (userId != null) {
            return "rate-limit:api:user:" + userId;
        }

        return "rate-limit:api:ip:" + resolveClientIp(request);
    }

    public String resolveWebSocketConnectionKey(String userId) {
        return "rate-limit:ws:connection:user:" + userId;
    }

    public String resolveWebSocketMessageKey(String userId) {
        return "rate-limit:ws:message:user:" + userId;
    }

    public String resolveLlmKey(String userId) {
        return "rate-limit:llm:user:" + userId;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
