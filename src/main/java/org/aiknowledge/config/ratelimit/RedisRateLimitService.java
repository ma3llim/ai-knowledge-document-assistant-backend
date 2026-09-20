package org.aiknowledge.config.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisRateLimitService {
    private final StringRedisTemplate redisTemplate;

    public RateLimitResult check(String key, int limit, int windowSeconds) {
        Long currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount == null) {
            throw new IllegalStateException("Failed to increment Redis rate-limit counter");
        }

        if (currentCount == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        if (currentCount > limit) {
            Long ttl = redisTemplate.getExpire(key);

            long retryAfterSeconds = ttl != null && ttl > 0 ? ttl : windowSeconds;

            return RateLimitResult.rejected(currentCount, limit, retryAfterSeconds);
        }

        return RateLimitResult.allowed(currentCount, limit);
    }
}
