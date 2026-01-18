package com.jwt.auth.auth_jwt.service.impl;

import com.jwt.auth.auth_jwt.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis Service Implementation
 * 
 * Provides Redis operations with error handling and logging
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== String Operations ====================

    @Override
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Set key: {}", key);
        } catch (Exception e) {
            log.error("Error setting key: {}", key, e);
            throw new RuntimeException("Failed to set Redis key: " + key, e);
        }
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
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
    public Long delete(Collection<String> keys) {
        try {
            Long result = redisTemplate.delete(keys);
            log.debug("Deleted {} keys", result);
            return result;
        } catch (Exception e) {
            log.error("Error deleting keys", e);
            return 0L;
        }
    }

    @Override
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Error checking key existence: {}", key, e);
            return false;
        }
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting expiration for key: {}", key, e);
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

    // ==================== Hash Operations ====================

    @Override
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            log.debug("Set hash field: {}:{}", key, field);
        } catch (Exception e) {
            log.error("Error setting hash field: {}:{}", key, field, e);
            throw new RuntimeException("Failed to set hash field", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String field, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForHash().get(key, field);
            if (value == null) {
                return null;
            }
            return (T) value;
        } catch (Exception e) {
            log.error("Error getting hash field: {}:{}", key, field, e);
            return null;
        }
    }

    @Override
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Error getting all hash fields: {}", key, e);
            return new HashMap<>();
        }
    }

    @Override
    public Long hDelete(String key, Object... fields) {
        try {
            return redisTemplate.opsForHash().delete(key, fields);
        } catch (Exception e) {
            log.error("Error deleting hash fields: {}", key, e);
            return 0L;
        }
    }

    @Override
    public Boolean hHasKey(String key, String field) {
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (Exception e) {
            log.error("Error checking hash field existence: {}:{}", key, field, e);
            return false;
        }
    }

    @Override
    public Set<Object> hKeys(String key) {
        try {
            return redisTemplate.opsForHash().keys(key);
        } catch (Exception e) {
            log.error("Error getting hash keys: {}", key, e);
            return new HashSet<>();
        }
    }

    @Override
    public Long hSize(String key) {
        try {
            return redisTemplate.opsForHash().size(key);
        } catch (Exception e) {
            log.error("Error getting hash size: {}", key, e);
            return 0L;
        }
    }

    // ==================== Set Operations ====================

    @Override
    public Long sAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("Error adding to set: {}", key, e);
            return 0L;
        }
    }

    @Override
    public Set<Object> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Error getting set members: {}", key, e);
            return new HashSet<>();
        }
    }

    @Override
    public Boolean sIsMember(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("Error checking set membership: {}", key, e);
            return false;
        }
    }

    @Override
    public Long sSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("Error getting set size: {}", key, e);
            return 0L;
        }
    }

    @Override
    public Long sRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error("Error removing from set: {}", key, e);
            return 0L;
        }
    }

    // ==================== List Operations ====================

    @Override
    public Long lPush(String key, Object value) {
        try {
            return redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception e) {
            log.error("Error left pushing to list: {}", key, e);
            return 0L;
        }
    }

    @Override
    public Long rPush(String key, Object value) {
        try {
            return redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.error("Error right pushing to list: {}", key, e);
            return 0L;
        }
    }

    @Override
    public List<Object> lRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("Error getting list range: {}", key, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Long lSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("Error getting list size: {}", key, e);
            return 0L;
        }
    }

    @Override
    public Long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            log.error("Error removing from list: {}", key, e);
            return 0L;
        }
    }

    // ==================== Pattern Operations ====================

    @Override
    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("Error getting keys with pattern: {}", pattern, e);
            return new HashSet<>();
        }
    }

    @Override
    public Long deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                return redisTemplate.delete(keys);
            }
            return 0L;
        } catch (Exception e) {
            log.error("Error deleting keys with pattern: {}", pattern, e);
            return 0L;
        }
    }

    // ==================== Cache-Specific Operations ====================

    @Override
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Error incrementing key: {}", key, e);
            return 0L;
        }
    }

    @Override
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Error incrementing key by delta: {}", key, e);
            return 0L;
        }
    }

    @Override
    public Long decrement(String key) {
        try {
            return redisTemplate.opsForValue().decrement(key);
        } catch (Exception e) {
            log.error("Error decrementing key: {}", key, e);
            return 0L;
        }
    }

    @Override
    public Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("Error decrementing key by delta: {}", key, e);
            return 0L;
        }
    }

    @Override
    public Boolean setIfAbsent(String key, Object value, Duration duration) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, duration);
        } catch (Exception e) {
            log.error("Error setting if absent: {}", key, e);
            return false;
        }
    }

    @Override
    public Object getAndSet(String key, Object value) {
        try {
            return redisTemplate.opsForValue().getAndSet(key, value);
        } catch (Exception e) {
            log.error("Error getting and setting key: {}", key, e);
            return null;
        }
    }
}
