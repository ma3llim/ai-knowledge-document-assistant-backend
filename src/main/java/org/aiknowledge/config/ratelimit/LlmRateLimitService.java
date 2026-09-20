package org.aiknowledge.config.ratelimit;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LlmRateLimitService {
    private final RedisRateLimitService redisRateLimitService;
    private final RateLimitKeyResolver keyResolver;
    private final AppProperties properties;

    public void check(String userId) {
        AppProperties.RateLimit.Llm llm = properties.rateLimit().llm();

        if (!llm.enabled()) {
            return;
        }

        String key = keyResolver.resolveLlmKey(userId);

        RateLimitResult result = redisRateLimitService.check(key, llm.requestsPerWindow(), llm.windowSeconds());

        if (!result.allowed()) {
            throw new RateLimitExceededException("Too many LLM requests. Please try again later.");
        }
    }
}
