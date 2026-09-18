package org.aiknowledge.config.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class RateLimitKeyResolver {
    public String resolveApiKey(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");

        if (userId != null) {
            return "ai-assistant:api:user:" + userId;
        }

        return "ai-assistant:api:ip-" + normalizeIp(resolveClientIp(request));
    }

    public String resolveWebSocketConnectionKey(String userId) {
        return "ai-assistant:ws:connection:user:" + userId;
    }

    public String resolveWebSocketMessageKey(String userId) {
        return "ai-assistant:ws:message:user:" + userId;
    }

    public String resolveLlmKey(String userId) {
        return "ai-assistant:llm:user:" + userId;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private String normalizeIp(String ip) {
        return ip.replace(":", "-");
    }
}
