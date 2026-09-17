package com.taskmanager.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publishesDocumentedRoutesParametersResponsesAndBearerScheme()
            throws Exception {
        byte[] content = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        JsonNode document = objectMapper.readTree(content);

        assertEquals(
                "Task Manager API",
                document.at("/info/title").asText()
        );
        assertEquals(
                "http",
                document.at("/components/securitySchemes/bearerAuth/type").asText()
        );
        assertEquals(
                "bearer",
                document.at("/components/securitySchemes/bearerAuth/scheme").asText()
        );

        JsonNode paths = document.get("paths");
        assertTrue(paths.has("/api/auth/register"));
        assertTrue(paths.has("/api/auth/login"));
        assertTrue(paths.has("/api/users/me"));
        assertTrue(paths.has("/api/tasks"));
        assertTrue(paths.has("/api/tasks/stats"));
        assertTrue(paths.has("/api/tasks/{id}"));

        JsonNode listOperation = paths.get("/api/tasks").get("get");
        Set<String> parameterNames = new HashSet<>();
        listOperation.get("parameters").forEach(parameter ->
                parameterNames.add(parameter.get("name").asText())
        );
        assertTrue(parameterNames.containsAll(Set.of(
                "search", "status", "priority", "page", "size", "sort"
        )));
        assertTrue(listOperation.get("responses").has("200"));
        assertTrue(listOperation.get("responses").has("400"));
        assertTrue(listOperation.get("responses").has("401"));

        JsonNode createSecurity = paths.get("/api/tasks")
                .get("post")
                .get("security");
        assertTrue(createSecurity.get(0).has("bearerAuth"));
        assertTrue(paths.get("/api/users/me")
                .get("get")
                .get("security")
                .get(0)
                .has("bearerAuth"));
        assertTrue(paths.get("/api/tasks/{id}")
                .get("delete")
                .get("responses")
                .has("204"));
    }
}
