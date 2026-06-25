package com.example.jwtrefresh.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-validity}") long accessTokenValidity,
            @Value("${app.jwt.refresh-token-validity}") long refreshTokenValidity
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    // 1. Access Token 생성
    public String createAccessToken(String email) {
        return createToken(email, accessTokenValidity);
    }

    // 2. Refresh Token 생성
    public String createRefreshToken(String email) {
        return createToken(email, refreshTokenValidity);
    }

    private String createToken(String email, long validity) {
        Claims claims = Jwts.claims().setSubject(email).build();
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validity))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            // 1. 토큰의 서명(Signature)을 검증하고 파싱 시도
            Jwts.parser()
                    .verifyWith(secretKey) // 설정한 Secret Key로 서명 검증
                    .build()
                    .parseSignedClaims(token);

            // 2. 예외가 발생하지 않으면 유효한 토큰
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            // 서명이 올바르지 않거나 토큰 형식이 잘못된 경우
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            // 토큰이 만료된 경우
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            // 지원하지 않는 형식의 토큰인 경우
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            // 토큰이 비어있거나 잘못된 경우
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public long getRefreshTokenValidity() {
        return refreshTokenValidity;
    }
}