package com.jwt.auth.auth_jwt.service;

import com.jwt.auth.auth_jwt.dto.request.LoginRequest;
import com.jwt.auth.auth_jwt.dto.request.SignUpRequest;
import com.jwt.auth.auth_jwt.dto.request.TokenRefreshRequest;
import com.jwt.auth.auth_jwt.dto.response.ApiResponse;
import com.jwt.auth.auth_jwt.dto.response.JwtAuthenticationResponse;
import com.jwt.auth.auth_jwt.dto.response.TokenRefreshResponse;
import com.jwt.auth.auth_jwt.entity.User;

public interface AuthService {

    JwtAuthenticationResponse login(LoginRequest loginRequest);

    User register(SignUpRequest signUpRequest);

    TokenRefreshResponse refreshToken(TokenRefreshRequest request);

    void logout(String refreshToken);
}
