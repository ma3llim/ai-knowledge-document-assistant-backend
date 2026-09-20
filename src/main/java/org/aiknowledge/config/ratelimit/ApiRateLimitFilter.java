package org.aiknowledge.config.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiRateLimitFilter extends OncePerRequestFilter {
    private final RedisRateLimitService rateLimitService;
    private final RateLimitKeyResolver keyResolver;
    private final AppProperties rateLimitProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        AppProperties.RateLimit.Api api = rateLimitProperties.rateLimit().api();
        if (!api.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = keyResolver.resolveApiKey(request);

        RateLimitResult result = rateLimitService.check(key, api.requestsPerWindow(), api.windowSeconds());

        long remaining = Math.max(0, api.requestsPerWindow() - result.currentCount());

        response.setHeader("X-RateLimit-Limit", String.valueOf(api.requestsPerWindow()));

        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

        if (!result.allowed()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));

            ApiErrorResponse errorResponse = ApiErrorResponse.builder().success(false)
                    .message("Too many requests. Please try again later.")
                    .errorCode("RATE_LIMIT_EXCEEDED")
                    .path(request.getRequestURI())
                    .build();

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            objectMapper.writeValue(response.getWriter(), errorResponse);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
