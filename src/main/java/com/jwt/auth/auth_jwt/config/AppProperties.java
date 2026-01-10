package com.jwt.auth.auth_jwt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Security security;

    @Getter
    @Setter
    public static class Security {
        private List<String> allowedOrigins;
        private List<String> publicEndpoints;
    }
}
