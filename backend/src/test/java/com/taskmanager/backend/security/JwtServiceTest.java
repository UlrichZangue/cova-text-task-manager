package com.taskmanager.backend.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET =
            "test-secret-key-that-is-long-enough-for-hmac-sha-256";

    @Test
    void generatedTokenContainsEmailAndIsValid() {
        JwtService jwtService = new JwtService(SECRET, 60_000);

        String token = jwtService.generateToken("alice@example.com");

        assertEquals("alice@example.com", jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, "alice@example.com"));
        assertFalse(jwtService.isTokenValid(token, "bob@example.com"));
        assertEquals(60L, jwtService.getExpirationSeconds());
    }
}
