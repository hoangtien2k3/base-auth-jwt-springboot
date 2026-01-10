package com.jwt.auth.auth_jwt.service.impl;

import com.jwt.auth.auth_jwt.dto.request.LoginRequest;
import com.jwt.auth.auth_jwt.dto.request.SignUpRequest;
import com.jwt.auth.auth_jwt.dto.request.TokenRefreshRequest;
import com.jwt.auth.auth_jwt.dto.response.JwtAuthenticationResponse;
import com.jwt.auth.auth_jwt.dto.response.TokenRefreshResponse;
import com.jwt.auth.auth_jwt.entity.RefreshToken;
import com.jwt.auth.auth_jwt.entity.Role;
import com.jwt.auth.auth_jwt.entity.User;
import com.jwt.auth.auth_jwt.exception.ResourceNotFoundException;
import com.jwt.auth.auth_jwt.exception.TokenRefreshException;
import com.jwt.auth.auth_jwt.repository.RoleRepository;
import com.jwt.auth.auth_jwt.repository.UserRepository;
import com.jwt.auth.auth_jwt.security.JwtTokenProvider;
import com.jwt.auth.auth_jwt.security.UserPrincipal;
import com.jwt.auth.auth_jwt.service.AuthService;
import com.jwt.auth.auth_jwt.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .id(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .roles(roles)
                .tokenType(Constants.TOKEN_PREFIX.trim())
                .build();
    }

    @Override
    @Transactional
    public User register(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email Address already in use!");
        }
        User user = User.builder()
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .phoneNumber(signUpRequest.getPhoneNumber())
                .isEmailVerified(true)
                .isAccountLocked(false)
                .build();
        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();
        if (strRoles == null) {
            Role userRole = roleRepository.findByName(Constants.ROLE_USER)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", Constants.ROLE_USER));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                Role adminRole = roleRepository.findByName(role)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", role));
                roles.add(adminRole);
            });
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = tokenProvider.generateTokenFromUserId(user.getId());
                    return TokenRefreshResponse.builder()
                            .accessToken(token)
                            .refreshToken(requestRefreshToken)
                            .tokenType(Constants.TOKEN_PREFIX.trim())
                            .build();
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }
}
