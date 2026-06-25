package com.example.jwtrefresh.backend.service;

import com.example.jwtrefresh.backend.dto.LoginRequest;
import com.example.jwtrefresh.backend.domain.RefreshToken;
import com.example.jwtrefresh.backend.dto.RegisterRequest;
import com.example.jwtrefresh.backend.domain.User;
import com.example.jwtrefresh.backend.dto.TokenResponse;
import com.example.jwtrefresh.backend.repository.RefreshTokenRepository;
import com.example.jwtrefresh.backend.repository.UserRepository;
import com.example.jwtrefresh.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder; // BCrypt 사용 권장

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 1. 유저 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        // 4. Refresh Token 저장/갱신 (Rotation 핵심)
        RefreshToken rt = refreshTokenRepository.findByUser(user)
                .orElse(new RefreshToken(user));
        rt.updateToken(refreshToken, jwtTokenProvider.getRefreshTokenValidity());
        refreshTokenRepository.save(rt);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 암호화 후 저장
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getEmail(), encodedPassword);
        userRepository.save(user);
    }

    @Transactional
    public TokenResponse refresh(String oldRefreshToken) {
        // 1. DB에서 토큰 조회
        RefreshToken rt = refreshTokenRepository.findByToken(oldRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 토큰입니다."));

        // 2. [보안 핵심] 이미 무효화된 토큰을 사용하려 함 -> 탈취 의심!
        if (rt.isRevoked()) {
            // 이 사용자의 모든 RefreshToken을 삭제하여 강제 로그아웃
            refreshTokenRepository.deleteByUser(rt.getUser());
            throw new IllegalStateException("재사용된 토큰입니다. 보안을 위해 강제 로그아웃합니다.");
        }

        // 3. 기존 토큰 무효화
        rt.revoke();

        // 4. 새로운 토큰 발급 (Rotation)
        String newAccessToken = jwtTokenProvider.createAccessToken(rt.getUser().getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(rt.getUser().getEmail());

        // 5. 새로운 토큰을 DB에 저장
        RefreshToken newRt = new RefreshToken(rt.getUser());
        newRt.updateToken(newRefreshToken, jwtTokenProvider.getRefreshTokenValidity());
        refreshTokenRepository.save(newRt);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

}