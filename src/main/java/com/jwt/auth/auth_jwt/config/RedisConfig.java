package com.jwt.auth.auth_jwt.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Configuration
 * <p>
 * This configuration class sets up Redis for:
 * 1. Caching (User, Role, Permission data)
 * 2. Session Management
 * 3. Rate Limiting
 * 4. Refresh Token Storage
 * <p>
 * IMPORTANT: This uses a separate ObjectMapper instance that does NOT affect
 * HTTP serialization
 */
@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig {

    private final CacheProperties cacheProperties;

    /**
     * Configure RedisTemplate for general Redis operations
     * Uses String serializer for both keys and values to avoid ObjectMapper
     * conflicts
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for everything
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        log.info("RedisTemplate configured with StringRedisSerializer");
        return template;
    }

    /**
     * Configure CacheManager with different TTL for different cache types
     * Uses a completely isolated ObjectMapper that won't affect HTTP serialization
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        if (!cacheProperties.isEnabled()) {
            log.warn("Cache is disabled in configuration");
        }

        // Create a completely isolated ObjectMapper for Redis cache ONLY
        ObjectMapper cacheObjectMapper = new ObjectMapper();
        cacheObjectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        cacheObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        cacheObjectMapper.registerModule(new JavaTimeModule());

        // Create Jackson serializer with the isolated ObjectMapper (using constructor
        // to avoid deprecation)
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
                cacheObjectMapper, Object.class);

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jackson2JsonRedisSerializer))
                .disableCachingNullValues();

        // Specific cache configurations with custom TTL
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put("users", defaultConfig
                .entryTtl(Duration.ofMillis(cacheProperties.getTtl().getUser())));

        cacheConfigurations.put("roles", defaultConfig
                .entryTtl(Duration.ofMillis(cacheProperties.getTtl().getRole())));

        cacheConfigurations.put("permissions", defaultConfig
                .entryTtl(Duration.ofMillis(cacheProperties.getTtl().getPermission())));

        cacheConfigurations.put("refreshTokens", defaultConfig
                .entryTtl(Duration.ofMillis(cacheProperties.getTtl().getRefreshToken())));

        log.info("Redis CacheManager configured with isolated ObjectMapper");
        log.info("Cache TTL - Users: {}ms, Roles: {}ms, Permissions: {}ms, RefreshTokens: {}ms",
                cacheProperties.getTtl().getUser(),
                cacheProperties.getTtl().getRole(),
                cacheProperties.getTtl().getPermission(),
                cacheProperties.getTtl().getRefreshToken());

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
