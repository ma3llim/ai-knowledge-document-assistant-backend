package org.aiknowledge.websocket;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.config.ratelimit.RateLimitKeyResolver;
import org.aiknowledge.config.ratelimit.RedisRateLimitService;
import org.aiknowledge.entity.User;
import org.aiknowledge.repository.UserRepository;
import org.aiknowledge.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RedisRateLimitService rateLimitService;
    private final RateLimitKeyResolver keyResolver;
    private final AppProperties properties;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        AppProperties.RateLimit.Websocket websocket = properties.rateLimit().websocket();
        String query = request.getURI().getQuery();

        if (query == null || query.isBlank()) {
            log.warn("WebSocket connection rejected: JWT token missing");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String token = extractToken(query);

        if (token == null || token.isBlank()) {
            log.warn("WebSocket connection rejected: JWT token missing");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        try {
            Claims claims = jwtService.extractClaims(token);

            if (!jwtService.isAccessToken(claims)) {
                log.warn("WebSocket connection rejected: invalid access token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            UUID userId = jwtService.getUserId(claims);

            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                log.warn("WebSocket connection rejected: user not found. userId={}", userId);
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            if (websocket.enabled()) {
                AppProperties.RateLimit.Limit connectionLimit = websocket.connections();

                String rateLimitKey = keyResolver.resolveWebSocketConnectionKey(userId.toString());

                var result = rateLimitService.check(rateLimitKey, connectionLimit.requestsPerWindow(),
                        connectionLimit.windowSeconds());

                if (!result.allowed()) {
                    log.warn("WebSocket connection rate limit exceeded. userId={}, retryAfterSeconds={}",
                            userId, result.retryAfterSeconds());

                    response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

                    response.getHeaders().add("Retry-After", String.valueOf(result.retryAfterSeconds()));
                    return false;
                }
            }

            attributes.put("user", user);
            attributes.put("userId", userId);

            log.info("WebSocket authentication successful. userId={}", userId);

            return true;
        } catch (Exception exception) {
            log.warn("WebSocket authentication failed", exception);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.warn("WebSocket handshake failed", exception);
        }
    }

    private String extractToken(String query) {
        for (String parameter : query.split("&")) {
            String[] parts = parameter.split("=", 2);

            if (parts.length == 2 && parts[0].equals("token")) {
                return parts[1];
            }
        }

        return null;
    }
}
