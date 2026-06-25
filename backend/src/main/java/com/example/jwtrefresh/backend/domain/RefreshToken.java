package com.example.jwtrefresh.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private boolean isRevoked = false;

    public RefreshToken(User user) {
        this.user = user;
    }

    public void updateToken(String token, long refreshTokenValidity) {
        this.token = token;
        this.expiryDate = LocalDateTime.now().plus(refreshTokenValidity, ChronoUnit.MILLIS);
    }

    public void revoke() {
        this.isRevoked = true;
    }
}