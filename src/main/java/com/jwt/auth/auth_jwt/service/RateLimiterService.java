package com.jwt.auth.auth_jwt.service;

/**
 * Rate Limiter Service Interface
 * 
 * Implements Token Bucket algorithm using Redis
 */
public interface RateLimiterService {

    /**
     * Check if request is allowed under rate limit
     * 
     * @param key      Unique identifier for the rate limit (e.g., "login:user123")
     * @param capacity Maximum number of requests allowed
     * @param duration Time window in seconds
     * @return true if request is allowed, false otherwise
     */
    boolean isAllowed(String key, int capacity, int duration);

    /**
     * Get remaining requests for a key
     * 
     * @param key Unique identifier
     * @return Number of remaining requests
     */
    long getRemainingRequests(String key, int capacity);

    /**
     * Get time until rate limit reset
     * 
     * @param key Unique identifier
     * @return Seconds until reset
     */
    long getTimeUntilReset(String key);

    /**
     * Reset rate limit for a key
     * 
     * @param key Unique identifier
     */
    void reset(String key);
}
