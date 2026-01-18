package com.jwt.auth.auth_jwt.exception;

/**
 * Exception thrown when rate limit is exceeded
 */
public class RateLimitExceededException extends RuntimeException {

    private final String key;
    private final int retryAfter;

    public RateLimitExceededException(String key, int retryAfter) {
        super(String.format("Rate limit exceeded for key: %s. Retry after %d seconds", key, retryAfter));
        this.key = key;
        this.retryAfter = retryAfter;
    }

    public RateLimitExceededException(String message, String key, int retryAfter) {
        super(message);
        this.key = key;
        this.retryAfter = retryAfter;
    }

    public String getKey() {
        return key;
    }

    public int getRetryAfter() {
        return retryAfter;
    }
}
