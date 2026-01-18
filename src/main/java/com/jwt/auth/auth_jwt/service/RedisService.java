package com.jwt.auth.auth_jwt.service;

import java.util.concurrent.TimeUnit;

public interface RedisService {

    /**
     * Set value with key
     */
    void set(String key, Object value);

    /**
     * Set value with key and expiration time
     */
    void set(String key, Object value, long timeout, TimeUnit unit);

    /**
     * Get value by key
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * Get value by key (generic)
     */
    Object get(String key);

    /**
     * Delete key
     */
    Boolean delete(String key);
    /**
     * Get expiration time for key
     */
    Long getExpire(String key, TimeUnit unit);

    /**
     * Increment value
     */
    Long increment(String key);
}
