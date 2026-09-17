package com.taskmanager.backend.task.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequestHasNoViolations() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Prepare the report");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void blankTitleIsRejected() {
        TaskRequest request = new TaskRequest();
        request.setTitle("   ");

        assertEquals("title", validator.validate(request).iterator().next()
                .getPropertyPath().toString());
    }

    @Test
    void titleLongerThanColumnLimitIsRejected() {
        TaskRequest request = new TaskRequest();
        request.setTitle("a".repeat(151));

        assertEquals("title", validator.validate(request).iterator().next()
                .getPropertyPath().toString());
    }
}
