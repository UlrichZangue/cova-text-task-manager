package com.taskmanager.backend.user.service;

import com.taskmanager.backend.exception.UnauthorizedException;
import com.taskmanager.backend.user.dto.UserProfileResponse;
import com.taskmanager.backend.user.entity.User;
import com.taskmanager.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getProfile(String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new UnauthorizedException(
                        "Authenticated user no longer exists"
                ));

        return new UserProfileResponse(
                user.getId(), user.getName(), user.getEmail(), user.getCreatedAt()
        );
    }
}
