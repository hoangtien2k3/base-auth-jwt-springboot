package com.jwt.auth.auth_jwt.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final String key;
    private final int retryAfter;

    public RateLimitExceededException(String message, String key, int retryAfter) {
        super(message);
        this.key = key;
        this.retryAfter = retryAfter;
    }
}
