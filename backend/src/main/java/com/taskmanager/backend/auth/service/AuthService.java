package com.taskmanager.backend.auth.service;

import com.taskmanager.backend.auth.dto.AuthResponse;
import com.taskmanager.backend.auth.dto.LoginRequest;
import com.taskmanager.backend.auth.dto.RegisterRequest;
import com.taskmanager.backend.auth.dto.RegisterResponse;
import com.taskmanager.backend.exception.BadRequestException;
import com.taskmanager.backend.exception.ConflictException;
import com.taskmanager.backend.exception.UnauthorizedException;
import com.taskmanager.backend.security.JwtService;
import com.taskmanager.backend.user.entity.User;
import com.taskmanager.backend.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password confirmation does not match");
        }

        String normalizedEmail = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        try {
            User savedUser = userRepository.save(user);
            return new RegisterResponse(
                    savedUser.getId(),
                    "User registered successfully"
            );
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Email is already registered");
        }
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizedEmail,
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException exception) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtService.generateToken(normalizedEmail);
        return new AuthResponse(token, jwtService.getExpirationSeconds());
    }
}
