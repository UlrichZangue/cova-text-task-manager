package com.taskmanager.backend.auth.service;

import com.taskmanager.backend.auth.dto.RegisterRequest;
import com.taskmanager.backend.auth.dto.RegisterResponse;
import com.taskmanager.backend.exception.BadRequestException;
import com.taskmanager.backend.exception.ConflictException;
import com.taskmanager.backend.user.entity.User;
import com.taskmanager.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void registerNormalizesEmailAndHashesPassword() {
        RegisterRequest request = request(
                "  Alice Example  ",
                "  Alice@Example.COM ",
                "Password123!",
                "Password123!"
        );
        UUID userId = UUID.randomUUID();

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded-password");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(userId);
                    return user;
                });

        RegisterResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(userId, response.getUserId());
        assertEquals("User registered successfully", response.getMessage());
        assertEquals("Alice Example", savedUser.getName());
        assertEquals("alice@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
    }

    @Test
    void registerRejectsDifferentPasswordConfirmation() {
        RegisterRequest request = request(
                "Alice",
                "alice@example.com",
                "Password123!",
                "Different123!"
        );

        assertThrows(BadRequestException.class, () -> authService.register(request));

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registerRejectsAnExistingNormalizedEmail() {
        RegisterRequest request = request(
                "Alice",
                " Alice@Example.COM ",
                "Password123!",
                "Password123!"
        );
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registerMapsDatabaseUniquenessRaceToConflict() {
        RegisterRequest request = request(
                "Alice",
                "alice@example.com",
                "Password123!",
                "Password123!"
        );
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded-password");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    private RegisterRequest request(
            String name,
            String email,
            String password,
            String confirmPassword
    ) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        return request;
    }
}
