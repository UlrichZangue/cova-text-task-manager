package com.taskmanager.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecurityErrorHandlerTest {

    @Autowired
    private SecurityErrorHandler securityErrorHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void returnsStructuredUnauthorizedResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/tasks");
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityErrorHandler.commence(
                request,
                response,
                new BadCredentialsException("Invalid token")
        );

        JsonNode body = objectMapper.readTree(response.getContentAsByteArray());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(body.get("status").asInt()).isEqualTo(401);
        assertThat(body.get("error").asText()).isEqualTo("UNAUTHORIZED");
        assertThat(body.get("message").asText()).isEqualTo("Authentication is required");
        assertThat(body.get("path").asText()).isEqualTo("/api/tasks");
    }

    @Test
    void returnsStructuredForbiddenResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityErrorHandler.handle(
                request,
                response,
                new AccessDeniedException("Forbidden")
        );

        JsonNode body = objectMapper.readTree(response.getContentAsByteArray());
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(body.get("status").asInt()).isEqualTo(403);
        assertThat(body.get("error").asText()).isEqualTo("FORBIDDEN");
        assertThat(body.get("message").asText()).isEqualTo("Access is denied");
        assertThat(body.get("path").asText()).isEqualTo("/api/admin");
    }
}
