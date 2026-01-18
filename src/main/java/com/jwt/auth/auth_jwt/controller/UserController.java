package com.jwt.auth.auth_jwt.controller;

import com.jwt.auth.auth_jwt.dto.response.ApiBaseResponse;
import com.jwt.auth.auth_jwt.dto.response.UserResponse;
import com.jwt.auth.auth_jwt.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    @GetMapping
    // @PreAuthorize("hasRole('ADMIN')")
    // @PreAuthorize("hasRole('USER') and hasRole('ADMIN')")
    // @PreAuthorize("!hasRole('USER')")
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieve all users with roles and permissions (Admin only)")
    public ResponseEntity<ApiBaseResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiBaseResponse.success(userService.getAllUsers()));
    }
}
