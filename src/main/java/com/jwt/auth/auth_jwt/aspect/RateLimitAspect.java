package com.jwt.auth.auth_jwt.aspect;

import com.jwt.auth.auth_jwt.annotation.RateLimit;
import com.jwt.auth.auth_jwt.exception.RateLimitExceededException;
import com.jwt.auth.auth_jwt.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * Rate Limit Aspect
 * 
 * Intercepts methods annotated with @RateLimit and enforces rate limiting
 * 
 * Uses AOP (Aspect-Oriented Programming) to separate rate limiting logic
 * from business logic
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimiterService rateLimiterService;

    /**
     * Around advice for @RateLimit annotation
     * 
     * Execution flow:
     * 1. Extract rate limit parameters from annotation
     * 2. Build unique key from annotation key + client identifier
     * 3. Check if request is allowed
     * 4. If allowed, proceed with method execution
     * 5. If not allowed, throw RateLimitExceededException
     */
    @Around("@annotation(com.jwt.auth.auth_jwt.annotation.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        if (rateLimit == null) {
            return joinPoint.proceed();
        }

        // Get client identifier (IP address or user ID)
        String clientId = getClientIdentifier();

        // Build rate limit key: {annotation_key}:{client_id}
        String rateLimitKey = rateLimit.key() + ":" + clientId;

        // Check rate limit
        boolean allowed = rateLimiterService.isAllowed(
                rateLimitKey,
                rateLimit.capacity(),
                rateLimit.duration());

        if (!allowed) {
            long timeUntilReset = rateLimiterService.getTimeUntilReset(rateLimitKey);
            log.warn("Rate limit exceeded for key: {}, client: {}, retry after: {}s",
                    rateLimit.key(), clientId, timeUntilReset);

            throw new RateLimitExceededException(
                    rateLimit.message(),
                    rateLimitKey,
                    (int) timeUntilReset);
        }

        // Log rate limit info
        long remaining = rateLimiterService.getRemainingRequests(rateLimitKey, rateLimit.capacity());
        log.debug("Rate limit check passed - Key: {}, Remaining: {}/{}, Duration: {}s",
                rateLimit.key(), remaining, rateLimit.capacity(), rateLimit.duration());

        return joinPoint.proceed();
    }

    /**
     * Get client identifier from request
     * Priority: X-Forwarded-For header > Remote address
     */
    private String getClientIdentifier() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            // Check for X-Forwarded-For header (for proxied requests)
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            // Fall back to remote address
            String remoteAddr = request.getRemoteAddr();
            return remoteAddr != null ? remoteAddr : "unknown";

        } catch (Exception e) {
            log.error("Error getting client identifier", e);
            return "unknown";
        }
    }
}
