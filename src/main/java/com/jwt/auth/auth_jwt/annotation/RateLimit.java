package com.jwt.auth.auth_jwt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Rate limit key (will be combined with user identifier)
     */
    String key();

    /**
     * Maximum number of requests allowed
     */
    int capacity() default 10;

    /**
     * Time window in seconds
     */
    int duration() default 60;

    /**
     * Error message when rate limit is exceeded
     */
    String message() default "Too many requests. Please try again later.";
}
