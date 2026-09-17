package com.taskmanager.backend.task.service;

import com.taskmanager.backend.exception.ResourceNotFoundException;
import com.taskmanager.backend.exception.UnauthorizedException;
import com.taskmanager.backend.exception.BadRequestException;
import com.taskmanager.backend.task.dto.PageResponse;
import com.taskmanager.backend.task.dto.TaskRequest;
import com.taskmanager.backend.task.dto.TaskResponse;
import com.taskmanager.backend.task.dto.TaskStatsResponse;
import com.taskmanager.backend.task.entity.Task;
import com.taskmanager.backend.task.entity.TaskPriority;
import com.taskmanager.backend.task.entity.TaskStatus;
import com.taskmanager.backend.task.mapper.TaskMapper;
import com.taskmanager.backend.task.repository.TaskRepository;
import com.taskmanager.backend.user.entity.User;
import com.taskmanager.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final String EMAIL = "alice@example.com";

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    private final TaskMapper taskMapper = new TaskMapper();

    private TaskService taskService;
    private User user;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, userRepository, taskMapper);
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    @Test
    void createsTaskForAuthenticatedUser() {
        TaskRequest request = request("First task");
        when(taskRepository.saveAndFlush(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task task = invocation.getArgument(0);
                    task.setId(UUID.randomUUID());
                    return task;
                });

        TaskResponse response = taskService.create(request, EMAIL);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).saveAndFlush(captor.capture());
        assertEquals(user, captor.getValue().getUser());
        assertEquals(TaskStatus.TODO, response.getStatus());
        assertEquals(TaskPriority.MEDIUM, response.getPriority());
    }

    @Test
    void listsOnlyActiveTasksForAuthenticatedUser() {
        Task first = task("First task");
        Task second = task("Second task");
        when(taskRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(
                List.of(first, second),
                PageRequest.of(0, 10),
                2
        ));

        PageResponse<TaskResponse> response = taskService.findAll(
                EMAIL,
                "task",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                0,
                10,
                "createdAt,desc"
        );

        assertEquals(List.of("First task", "Second task"), response.getContent().stream()
                .map(TaskResponse::getTitle)
                .toList());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(2, response.getTotalElements());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);
        verify(taskRepository).findAll(
                any(Specification.class),
                pageableCaptor.capture()
        );
        assertEquals("DESC", pageableCaptor.getValue().getSort()
                .getOrderFor("createdAt").getDirection().name());
    }

    @Test
    void returnsOwnedTask() {
        UUID taskId = UUID.randomUUID();
        Task task = task("Owned task");
        task.setId(taskId);
        when(taskRepository.findByIdAndUserAndDeletedAtIsNull(taskId, user))
                .thenReturn(Optional.of(task));

        TaskResponse response = taskService.findById(taskId, EMAIL);

        assertEquals(taskId, response.getId());
        assertEquals("Owned task", response.getTitle());
    }

    @Test
    void returnsStatsForAuthenticatedUser() {
        TaskStatsResponse expected = new TaskStatsResponse(7L, 3L, 2L, 2L);
        when(taskRepository.findStatsByUser(
                user,
                TaskStatus.TODO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.DONE
        )).thenReturn(expected);

        TaskStatsResponse response = taskService.getStats(EMAIL);

        assertEquals(7, response.getTotal());
        assertEquals(3, response.getTodo());
        assertEquals(2, response.getInProgress());
        assertEquals(2, response.getDone());
    }

    @Test
    void hidesMissingDeletedOrForeignTaskBehindNotFound() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findByIdAndUserAndDeletedAtIsNull(taskId, user))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.findById(taskId, EMAIL)
        );

        verify(taskRepository, never()).findById(taskId);
    }

    @Test
    void updatesOwnedTask() {
        UUID taskId = UUID.randomUUID();
        Task task = task("Old title");
        task.setId(taskId);
        TaskRequest request = request("New title");
        request.setStatus(TaskStatus.DONE);
        when(taskRepository.findByIdAndUserAndDeletedAtIsNull(taskId, user))
                .thenReturn(Optional.of(task));
        when(taskRepository.saveAndFlush(task)).thenReturn(task);

        TaskResponse response = taskService.update(taskId, request, EMAIL);

        assertEquals("New title", response.getTitle());
        assertEquals(TaskStatus.DONE, response.getStatus());
        verify(taskRepository).saveAndFlush(task);
    }

    @Test
    void softDeletesOwnedTask() {
        UUID taskId = UUID.randomUUID();
        Task task = task("Task to delete");
        task.setId(taskId);
        when(taskRepository.findByIdAndUserAndDeletedAtIsNull(taskId, user))
                .thenReturn(Optional.of(task));

        taskService.delete(taskId, EMAIL);

        assertNotNull(task.getDeletedAt());
        verify(taskRepository).save(task);
    }

    @Test
    void rejectsPrincipalWhoseUserNoLongerExists() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> taskService.findAll(
                        EMAIL, null, null, null, 0, 10, "createdAt,desc"
                )
        );

        assertTrue(exception.getMessage().contains("no longer exists"));
        verify(taskRepository, never()).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }

    @Test
    void rejectsInvalidPaginationAndSortParameters() {
        assertThrows(BadRequestException.class, () -> taskService.findAll(
                EMAIL, null, null, null, -1, 10, "createdAt,desc"
        ));
        assertThrows(BadRequestException.class, () -> taskService.findAll(
                EMAIL, null, null, null, 0, 101, "createdAt,desc"
        ));
        assertThrows(BadRequestException.class, () -> taskService.findAll(
                EMAIL, null, null, null, 0, 10, "deletedAt,desc"
        ));
        assertThrows(BadRequestException.class, () -> taskService.findAll(
                EMAIL, null, null, null, 0, 10, "createdAt,sideways"
        ));

        verify(taskRepository, never()).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }

    private TaskRequest request(String title) {
        TaskRequest request = new TaskRequest();
        request.setTitle(title);
        return request;
    }

    private Task task(String title) {
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle(title);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setUser(user);
        return task;
    }
}
