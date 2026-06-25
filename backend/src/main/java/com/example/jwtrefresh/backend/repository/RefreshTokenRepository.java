package com.example.jwtrefresh.backend.repository;

import com.example.jwtrefresh.backend.domain.RefreshToken;
import com.example.jwtrefresh.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // 특정 유저의 Refresh Token 조회
    Optional<RefreshToken> findByUser(User user);

    // 특정 토큰 문자열로 조회 (나중에 토큰 재발급 검증 시 사용)
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}