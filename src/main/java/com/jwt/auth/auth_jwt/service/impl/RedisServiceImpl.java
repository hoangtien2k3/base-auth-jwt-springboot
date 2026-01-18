package com.jwt.auth.auth_jwt.service.impl;

import com.jwt.auth.auth_jwt.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value) {
        try {
            // Convert value to String for StringRedisSerializer
            String strValue = value instanceof String ? (String) value : String.valueOf(value);
            redisTemplate.opsForValue().set(key, strValue);
            log.debug("Set key: {}", key);
        } catch (Exception e) {
            log.error("Error setting key: {}", key, e);
            throw new RuntimeException("Failed to set Redis key: " + key, e);
        }
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            // Convert value to String for StringRedisSerializer
            String strValue = value instanceof String ? (String) value : String.valueOf(value);
            redisTemplate.opsForValue().set(key, strValue, timeout, unit);
            log.debug("Set key: {} with expiration: {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting key with expiration: {}", key, e);
            throw new RuntimeException("Failed to set Redis key with expiration: " + key, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Key not found: {}", key);
                return null;
            }

            // Handle conversion if value is String but target is not String
            if (value instanceof String strVal) {
                if (clazz.equals(Long.class)) {
                    return (T) Long.valueOf(strVal);
                } else if (clazz.equals(Integer.class)) {
                    return (T) Integer.valueOf(strVal);
                } else if (clazz.equals(Double.class)) {
                    return (T) Double.valueOf(strVal);
                } else if (clazz.equals(Boolean.class)) {
                    return (T) Boolean.valueOf(strVal);
                }
            }

            return (T) value;
        } catch (Exception e) {
            log.error("Error getting key: {}", key, e);
            return null;
        }
    }

    @Override
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting key: {}", key, e);
            return null;
        }
    }

    @Override
    public Boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("Deleted key: {}, result: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Error deleting key: {}", key, e);
            return false;
        }
    }

    @Override
    public Long getExpire(String key, TimeUnit unit) {
        try {
            return redisTemplate.getExpire(key, unit);
        } catch (Exception e) {
            log.error("Error getting expiration for key: {}", key, e);
            return -1L;
        }
    }

    @Override
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Error incrementing key: {}", key, e);
            return 0L;
        }
    }
}
