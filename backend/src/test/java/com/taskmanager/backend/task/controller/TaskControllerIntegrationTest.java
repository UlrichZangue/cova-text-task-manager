package com.taskmanager.backend.task.controller;

import com.taskmanager.backend.security.JwtService;
import com.taskmanager.backend.task.entity.Task;
import com.taskmanager.backend.task.entity.TaskPriority;
import com.taskmanager.backend.task.entity.TaskStatus;
import com.taskmanager.backend.task.repository.TaskRepository;
import com.taskmanager.backend.user.entity.User;
import com.taskmanager.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User owner;
    private User otherUser;
    private String ownerToken;
    private String otherToken;

    @BeforeEach
    void setUpUsers() {
        owner = saveUser("owner");
        otherUser = saveUser("other");
        ownerToken = jwtService.generateToken(owner.getEmail());
        otherToken = jwtService.generateToken(otherUser.getEmail());
    }

    @Test
    void executesCompleteCrudAndSoftDeleteWorkflow() throws Exception {
        MvcResult creation = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Integration task",
                                  "description": "Created through MockMvc",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andReturn();
        String taskId = objectMapper.readTree(
                creation.getResponse().getContentAsByteArray()
        ).get("id").asText();

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(taskId));

        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integration task"));

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated integration task",
                                  "status": "DONE",
                                  "priority": "MEDIUM"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated integration task"))
                .andExpect(jsonPath("$.status").value("DONE"));

        mockMvc.perform(delete("/api/tasks/{id}", taskId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void preventsReadingUpdatingAndDeletingAnotherUsersTask() throws Exception {
        Task task = saveTask(owner, "Private task", TaskStatus.TODO,
                TaskPriority.HIGH, false);

        mockMvc.perform(get("/api/tasks/{id}", task.getId())
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/tasks/{id}", task.getId())
                        .header("Authorization", bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + "\"title\":\"Stolen task\"" + "}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/tasks/{id}", task.getId())
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isNotFound());

        org.junit.jupiter.api.Assertions.assertNull(
                taskRepository.findById(task.getId()).orElseThrow().getDeletedAt()
        );
    }

    @Test
    void combinesSearchFiltersPaginationAndSortWithoutLeakingTasks() throws Exception {
        saveTask(owner, "Rapport Alpha", TaskStatus.TODO,
                TaskPriority.HIGH, false);
        saveTask(owner, "Rapport Beta", TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH, false);
        saveTask(owner, "Meeting", TaskStatus.TODO,
                TaskPriority.LOW, false);
        saveTask(owner, "Deleted rapport", TaskStatus.TODO,
                TaskPriority.HIGH, true);
        saveTask(otherUser, "Foreign rapport", TaskStatus.TODO,
                TaskPriority.HIGH, false);

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", bearer(ownerToken))
                        .param("search", "RAPPORT")
                        .param("status", "TODO")
                        .param("priority", "HIGH")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Rapport Alpha"));

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", bearer(ownerToken))
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsStatsForOwnerAndIgnoresDeletedAndForeignTasks() throws Exception {
        saveTask(owner, "Todo", TaskStatus.TODO, TaskPriority.MEDIUM, false);
        saveTask(owner, "Progress", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, false);
        saveTask(owner, "Done", TaskStatus.DONE, TaskPriority.MEDIUM, false);
        saveTask(owner, "Deleted", TaskStatus.TODO, TaskPriority.MEDIUM, true);
        saveTask(otherUser, "Foreign", TaskStatus.TODO, TaskPriority.MEDIUM, false);

        mockMvc.perform(get("/api/tasks/stats")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.todo").value(1))
                .andExpect(jsonPath("$.inProgress").value(1))
                .andExpect(jsonPath("$.done").value(1));
    }

    private User saveUser(String prefix) {
        User user = new User();
        user.setName("Integration " + prefix);
        user.setEmail(prefix + "-" + UUID.randomUUID() + "@example.com");
        user.setPassword(passwordEncoder.encode("Password123!"));
        return userRepository.saveAndFlush(user);
    }

    private Task saveTask(
            User user,
            String title,
            TaskStatus status,
            TaskPriority priority,
            boolean deleted
    ) {
        Task task = new Task();
        task.setUser(user);
        task.setTitle(title);
        task.setDescription(title + " description");
        task.setStatus(status);
        task.setPriority(priority);
        if (deleted) {
            task.setDeletedAt(LocalDateTime.now());
        }
        return taskRepository.saveAndFlush(task);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
