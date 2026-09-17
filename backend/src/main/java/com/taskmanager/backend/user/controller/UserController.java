package com.taskmanager.backend.user.controller;

import com.taskmanager.backend.exception.ErrorResponse;
import com.taskmanager.backend.user.dto.UserProfileResponse;
import com.taskmanager.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Authenticated user profile")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get the authenticated user's profile",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile returned"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Missing, invalid or expired JWT",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public ResponseEntity<UserProfileResponse> getProfile(
            Authentication authentication
    ) {
        return ResponseEntity.ok(userService.getProfile(authentication.getName()));
    }
}
