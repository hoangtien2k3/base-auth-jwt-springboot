package com.jwt.auth.auth_jwt.service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis Service Interface
 * 
 * Provides comprehensive Redis operations for:
 * - String operations (get, set, delete)
 * - Hash operations (for complex objects)
 * - Set operations (for collections)
 * - List operations (for ordered data)
 * - Expiration management
 */
public interface RedisService {

    // ==================== String Operations ====================

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
     * Delete multiple keys
     */
    Long delete(Collection<String> keys);

    /**
     * Check if key exists
     */
    Boolean hasKey(String key);

    /**
     * Set expiration time for key
     */
    Boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * Get expiration time for key
     */
    Long getExpire(String key, TimeUnit unit);

    // ==================== Hash Operations ====================

    /**
     * Set hash field
     */
    void hSet(String key, String field, Object value);

    /**
     * Get hash field
     */
    <T> T hGet(String key, String field, Class<T> clazz);

    /**
     * Get all hash fields
     */
    Map<Object, Object> hGetAll(String key);

    /**
     * Delete hash field
     */
    Long hDelete(String key, Object... fields);

    /**
     * Check if hash field exists
     */
    Boolean hHasKey(String key, String field);

    /**
     * Get all hash keys
     */
    Set<Object> hKeys(String key);

    /**
     * Get hash size
     */
    Long hSize(String key);

    // ==================== Set Operations ====================

    /**
     * Add members to set
     */
    Long sAdd(String key, Object... values);

    /**
     * Get all members of set
     */
    Set<Object> sMembers(String key);

    /**
     * Check if member exists in set
     */
    Boolean sIsMember(String key, Object value);

    /**
     * Get set size
     */
    Long sSize(String key);

    /**
     * Remove members from set
     */
    Long sRemove(String key, Object... values);

    // ==================== List Operations ====================

    /**
     * Push value to list (left)
     */
    Long lPush(String key, Object value);

    /**
     * Push value to list (right)
     */
    Long rPush(String key, Object value);

    /**
     * Get list range
     */
    List<Object> lRange(String key, long start, long end);

    /**
     * Get list size
     */
    Long lSize(String key);

    /**
     * Remove value from list
     */
    Long lRemove(String key, long count, Object value);

    // ==================== Pattern Operations ====================

    /**
     * Get all keys matching pattern
     */
    Set<String> keys(String pattern);

    /**
     * Delete all keys matching pattern
     */
    Long deletePattern(String pattern);

    // ==================== Cache-Specific Operations ====================

    /**
     * Increment value
     */
    Long increment(String key);

    /**
     * Increment value by delta
     */
    Long increment(String key, long delta);

    /**
     * Decrement value
     */
    Long decrement(String key);

    /**
     * Decrement value by delta
     */
    Long decrement(String key, long delta);

    /**
     * Set if absent (only if key doesn't exist)
     */
    Boolean setIfAbsent(String key, Object value, Duration duration);

    /**
     * Get and set (atomic operation)
     */
    Object getAndSet(String key, Object value);
}
