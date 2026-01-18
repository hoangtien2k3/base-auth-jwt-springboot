package com.jwt.auth.auth_jwt.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
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
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Configuration
 * 
 * This configuration class sets up Redis for:
 * 1. Caching (User, Role, Permission data)
 * 2. Session Management
 * 3. Rate Limiting
 * 4. Refresh Token Storage
 */
@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig {

    private final CacheProperties cacheProperties;

    /**
     * Configure ObjectMapper for Redis JSON serialization
     * Supports Java 8 time types and polymorphic types
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        return mapper;
    }

    /**
     * Configure RedisTemplate for general Redis operations
     * Uses String for keys and JSON for values
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Use JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        log.info("RedisTemplate configured successfully");
        return template;
    }

    /**
     * Configure CacheManager with different TTL for different cache types
     * 
     * Cache Names:
     * - users: User entity cache (30 minutes)
     * - roles: Role entity cache (1 hour)
     * - permissions: Permission entity cache (1 hour)
     * - refreshTokens: Refresh token cache (7 days)
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        if (!cacheProperties.isEnabled()) {
            log.warn("Cache is disabled in configuration");
        }

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper())))
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

        log.info("Redis CacheManager configured with custom TTL settings");
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
