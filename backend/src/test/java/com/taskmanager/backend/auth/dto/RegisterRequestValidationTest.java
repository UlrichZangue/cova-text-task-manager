package com.taskmanager.backend.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequestHasNoViolations() {
        RegisterRequest request = validRequest();

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void requiredFieldsAndEmailFormatAreValidated() {
        RegisterRequest request = new RegisterRequest();
        request.setName(" ");
        request.setEmail("invalid-email");
        request.setPassword("short");
        request.setConfirmPassword(" ");

        Set<String> invalidFields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(
                Set.of("name", "email", "password", "confirmPassword"),
                invalidFields
        );
    }

    @Test
    void fieldLengthsAreValidated() {
        RegisterRequest request = validRequest();
        request.setName("a".repeat(101));
        request.setEmail("a".repeat(244) + "@example.com");
        request.setPassword("a".repeat(73));

        Set<String> invalidFields = validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());

        assertEquals(Set.of("name", "email", "password"), invalidFields);
    }

    private RegisterRequest validRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice Example");
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");
        return request;
    }
}
