package com.taskmanager.backend.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Map<String, String> fieldErrors;

    public ErrorResponse(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    public static ErrorResponse of(
            HttpStatus status,
            String message,
            String path
    ) {
        return of(status, message, path, Map.of());
    }

    public static ErrorResponse of(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        return new ErrorResponse(
                status.value(),
                status.name(),
                message,
                path,
                fieldErrors
        );
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
