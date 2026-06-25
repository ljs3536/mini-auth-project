package com.example.jwtrefresh.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccessTokenResponse {
    private String accessToken;
    private String refreshToken;

    public AccessTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public AccessTokenResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}