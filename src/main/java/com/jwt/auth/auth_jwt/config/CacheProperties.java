package com.jwt.auth.auth_jwt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    private Ttl ttl = new Ttl();
    private boolean enabled = true;

    @Data
    public static class Ttl {
        private long user = 1800000L; // 30 minutes
        private long role = 3600000L; // 1 hour
        private long permission = 3600000L; // 1 hour
        private long refreshToken = 604800000L; // 7 days
    }
}
