package com.jwt.auth.auth_jwt.service.impl;

import com.jwt.auth.auth_jwt.service.RateLimiterService;
import com.jwt.auth.auth_jwt.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Rate Limiter Service Implementation
 * 
 * Uses Token Bucket algorithm with Redis for distributed rate limiting
 * 
 * Algorithm:
 * 1. Each request consumes one token
 * 2. Tokens are replenished at a fixed rate
 * 3. If no tokens available, request is rejected
 * 
 * Redis Implementation:
 * - Key: rate_limit:{key}
 * - Value: Current token count
 * - Expiration: Duration of the time window
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterServiceImpl implements RateLimiterService {

    private final RedisService redisService;
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Override
    public boolean isAllowed(String key, int capacity, int duration) {
        String redisKey = RATE_LIMIT_PREFIX + key;

        try {
            // Get current count
            Long currentCount = redisService.get(redisKey, Long.class);

            if (currentCount == null) {
                // First request - initialize counter
                redisService.set(redisKey, 1L, duration, TimeUnit.SECONDS);
                log.debug("Rate limit initialized for key: {}, capacity: {}, duration: {}s",
                        key, capacity, duration);
                return true;
            }

            if (currentCount < capacity) {
                // Increment counter
                redisService.increment(redisKey);
                log.debug("Rate limit check passed for key: {}, current: {}/{}",
                        key, currentCount + 1, capacity);
                return true;
            }

            // Rate limit exceeded
            log.warn("Rate limit exceeded for key: {}, current: {}/{}",
                    key, currentCount, capacity);
            return false;

        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            // Fail open - allow request if Redis is down
            return true;
        }
    }

    @Override
    public long getRemainingRequests(String key, int capacity) {
        String redisKey = RATE_LIMIT_PREFIX + key;

        try {
            Long currentCount = redisService.get(redisKey, Long.class);
            if (currentCount == null) {
                return capacity;
            }
            return Math.max(0, capacity - currentCount);
        } catch (Exception e) {
            log.error("Error getting remaining requests for key: {}", key, e);
            return capacity;
        }
    }

    @Override
    public long getTimeUntilReset(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;

        try {
            Long ttl = redisService.getExpire(redisKey, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? ttl : 0;
        } catch (Exception e) {
            log.error("Error getting time until reset for key: {}", key, e);
            return 0;
        }
    }

    @Override
    public void reset(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;

        try {
            redisService.delete(redisKey);
            log.info("Rate limit reset for key: {}", key);
        } catch (Exception e) {
            log.error("Error resetting rate limit for key: {}", key, e);
        }
    }
}
