package com.example.jwtrefresh.backend.controller;

import com.example.jwtrefresh.backend.dto.AccessTokenResponse;
import com.example.jwtrefresh.backend.dto.LoginRequest;
import com.example.jwtrefresh.backend.dto.RegisterRequest;
import com.example.jwtrefresh.backend.dto.TokenResponse;
import com.example.jwtrefresh.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        TokenResponse tokens = authService.login(request);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true) // JS에서 접근 불가 (XSS 방어)
                .secure(true)   // HTTPS 사용 시 필수
                .path("/")
                .maxAge(604800) // 7일
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new AccessTokenResponse(tokens.getAccessToken()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("User Registered Successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refreshToken") String refreshToken) {
        TokenResponse newAccessToken = authService.refresh(refreshToken);
        return ResponseEntity.ok(newAccessToken);
    }
}