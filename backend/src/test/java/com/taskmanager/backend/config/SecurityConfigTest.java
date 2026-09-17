package com.taskmanager.backend.config;

import com.taskmanager.backend.security.JwtAuthenticationFilter;
import com.taskmanager.backend.security.SecurityErrorHandler;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void configuresOnlyDeclaredCorsOriginsAndHeaders() {
        SecurityConfig securityConfig = new SecurityConfig(
                mock(JwtAuthenticationFilter.class),
                mock(SecurityErrorHandler.class),
                "http://localhost:5173, https://app.example.com"
        );
        CorsConfigurationSource source =
                securityConfig.corsConfigurationSource();
        CorsConfiguration configuration = source.getCorsConfiguration(
                new MockHttpServletRequest("OPTIONS", "/api/tasks")
        );

        assertNotNull(configuration);
        assertEquals(
                java.util.List.of(
                        "http://localhost:5173",
                        "https://app.example.com"
                ),
                configuration.getAllowedOrigins()
        );
        assertEquals(
                java.util.List.of("Authorization", "Content-Type"),
                configuration.getAllowedHeaders()
        );
        assertFalse(configuration.getAllowCredentials());
    }
}
