package com.taskmanager.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void rejectsMissingOrWeakConfiguration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(" ", 60_000)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService("too-short", 60_000)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(SECRET, 0)
        );
    }

    @Test
    void rejectsExpiredToken() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        String expiredToken = Jwts.builder()
                .subject("alice@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(
                        SECRET.getBytes(StandardCharsets.UTF_8)
                ))
                .compact();

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.extractEmail(expiredToken)
        );
    }
}
