package com.jwt.auth.auth_jwt.repository;

import com.jwt.auth.auth_jwt.entity.RefreshToken;
import com.jwt.auth.auth_jwt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUser(User user);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false AND rt.expiresAt > CURRENT_TIMESTAMP")
    List<RefreshToken> findAllActiveTokenByUser(Long userId);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true, rt.revokedAt = CURRENT_TIMESTAMP WHERE rt.user.id = :userId AND rt.isRevoked = false")
    void revokeAllUserTokens(Long userId);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
