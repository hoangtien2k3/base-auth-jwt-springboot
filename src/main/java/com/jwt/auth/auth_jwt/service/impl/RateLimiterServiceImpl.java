package com.jwt.auth.auth_jwt.service.impl;

import com.jwt.auth.auth_jwt.service.RateLimiterService;
import com.jwt.auth.auth_jwt.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
            Long currentCount = redisService.get(redisKey, Long.class);
            if (currentCount == null) {
                redisService.set(redisKey, 1L, duration, TimeUnit.SECONDS);
                return true;
            }
            if (currentCount < capacity) {
                redisService.increment(redisKey);
                return true;
            }
            return false;
        } catch (Exception e) {
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
