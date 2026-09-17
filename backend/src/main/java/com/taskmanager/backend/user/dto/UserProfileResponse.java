package com.taskmanager.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Profile of the authenticated user")
public record UserProfileResponse(
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
        @Schema(example = "Ada Lovelace") String name,
        @Schema(example = "ada@example.com") String email,
        @Schema(example = "2026-09-17T18:00:00") LocalDateTime createdAt
) {
}
