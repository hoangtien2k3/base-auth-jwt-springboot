package com.jwt.auth.auth_jwt.controller;

import com.jwt.auth.auth_jwt.annotation.RateLimit;
import com.jwt.auth.auth_jwt.dto.request.LogOutRequest;
import com.jwt.auth.auth_jwt.dto.request.LoginRequest;
import com.jwt.auth.auth_jwt.dto.request.SignUpRequest;
import com.jwt.auth.auth_jwt.dto.request.TokenRefreshRequest;
import com.jwt.auth.auth_jwt.dto.response.ApiBaseResponse;
import com.jwt.auth.auth_jwt.dto.response.JwtAuthenticationResponse;
import com.jwt.auth.auth_jwt.dto.response.TokenRefreshResponse;
import com.jwt.auth.auth_jwt.entity.User;
import com.jwt.auth.auth_jwt.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return value token")
    @RateLimit(key = "login", capacity = 5, duration = 60, message = "Too many login attempts. Please try again after 1 minute.")
    public ResponseEntity<ApiBaseResponse<JwtAuthenticationResponse>> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiBaseResponse.success(authService.login(loginRequest)));
    }

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Register a new user")
    @RateLimit(key = "register", capacity = 3, duration = 300, message = "Too many registration attempts. Please try again after 5 minutes.")
    public ResponseEntity<ApiBaseResponse<Void>> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        User user = authService.register(signUpRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(user.getUsername()).toUri();
        return ResponseEntity.created(location)
                .body(ApiBaseResponse.created(null, "User registered successfully"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    @RateLimit(key = "refresh-token", capacity = 10, duration = 60, message = "Too many token refresh attempts. Please try again later.")
    public ResponseEntity<ApiBaseResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiBaseResponse.success(authService.refreshToken(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revoke refresh token")
    @RateLimit(key = "logout", capacity = 10, duration = 60, message = "Too many logout attempts. Please try again later.")
    public ResponseEntity<ApiBaseResponse<Void>> logout(@Valid @RequestBody LogOutRequest logOutRequest) {
        authService.logout(logOutRequest.getRefreshToken());
        return ResponseEntity.ok(ApiBaseResponse.success(null, "Log out successful"));
    }
}
