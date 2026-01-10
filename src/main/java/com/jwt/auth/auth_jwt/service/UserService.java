package com.jwt.auth.auth_jwt.service;

import com.jwt.auth.auth_jwt.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
}
