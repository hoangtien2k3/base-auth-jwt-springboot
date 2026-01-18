package com.jwt.auth.auth_jwt.service.impl;

import com.jwt.auth.auth_jwt.dto.response.UserResponse;
import com.jwt.auth.auth_jwt.entity.Role;
import com.jwt.auth.auth_jwt.entity.Permission;
import com.jwt.auth.auth_jwt.repository.UserRepository;
import com.jwt.auth.auth_jwt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'all_users'")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    Set<String> roles = user.getRoles().stream()
                            .map(Role::getName)
                            .collect(Collectors.toSet());
                    Set<String> permissions = user.getRoles().stream()
                            .flatMap(role -> role.getPermissions().stream())
                            .map(Permission::getName)
                            .collect(Collectors.toSet());
                    return UserResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .enabled(!user.isAccountLocked())
                            .roles(roles)
                            .permissions(permissions)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
