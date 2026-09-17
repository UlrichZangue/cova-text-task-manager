package com.taskmanager.backend.auth.dto;

public class AuthResponse {

    private final String token;
    private final String tokenType;
    private final long expiresIn;

    public AuthResponse(String token, long expiresIn) {
        this.token = token;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
