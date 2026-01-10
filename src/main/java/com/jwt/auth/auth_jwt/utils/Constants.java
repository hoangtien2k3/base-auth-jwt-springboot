package com.jwt.auth.auth_jwt.utils;

public class Constants {
    // Auth Routes
    public static final String AUTH_API_PREFIX = "/api/v1/auth";
    public static final String LOGIN_URL = AUTH_API_PREFIX + "/login";
    public static final String REGISTER_URL = AUTH_API_PREFIX + "/register";
    public static final String REFRESH_TOKEN_URL = AUTH_API_PREFIX + "/refresh-token";

    // Security
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    // Roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MODERATOR = "ROLE_MODERATOR";
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    // Errors
    public static final String ERROR_UNAUTHORIZED = "Unauthorized access";
    public static final String ERROR_FORBIDDEN = "Access denied";
    public static final String ERROR_NOT_FOUND = "Resource not found";
    public static final String ERROR_BAD_REQUEST = "Invalid request";

    private Constants() {
    } // Prevent instantiation
}
