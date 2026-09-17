package com.taskmanager.backend.auth.dto;

import java.util.UUID;

public class RegisterResponse {

    private final UUID userId;
    private final String message;

    public RegisterResponse(UUID userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }
}
