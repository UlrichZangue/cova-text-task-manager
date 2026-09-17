package com.taskmanager.backend.task.mapper;

import com.taskmanager.backend.task.dto.TaskRequest;
import com.taskmanager.backend.task.dto.TaskResponse;
import com.taskmanager.backend.task.entity.Task;
import com.taskmanager.backend.task.entity.TaskPriority;
import com.taskmanager.backend.task.entity.TaskStatus;
import com.taskmanager.backend.user.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TaskMapperTest {

    private final TaskMapper taskMapper = new TaskMapper();

    @Test
    void mapsCreationRequestAndAppliesDefaults() {
        TaskRequest request = new TaskRequest();
        request.setTitle("  Prepare the report  ");
        request.setDescription("Quarterly figures");
        request.setDueDate(LocalDate.of(2026, 9, 30));
        User user = new User();

        Task task = taskMapper.toEntity(request, user);

        assertEquals("Prepare the report", task.getTitle());
        assertEquals("Quarterly figures", task.getDescription());
        assertEquals(TaskStatus.TODO, task.getStatus());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertEquals(LocalDate.of(2026, 9, 30), task.getDueDate());
        assertSame(user, task.getUser());
    }

    @Test
    void updatesOnlyClientEditableTaskFields() {
        Task task = completeTask();
        User owner = task.getUser();
        UUID id = task.getId();
        LocalDateTime createdAt = task.getCreatedAt();
        LocalDateTime deletedAt = task.getDeletedAt();
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated task");
        request.setStatus(TaskStatus.DONE);
        request.setPriority(TaskPriority.HIGH);

        taskMapper.updateEntity(request, task);

        assertEquals(id, task.getId());
        assertSame(owner, task.getUser());
        assertEquals(createdAt, task.getCreatedAt());
        assertEquals(deletedAt, task.getDeletedAt());
        assertEquals("Updated task", task.getTitle());
        assertEquals(TaskStatus.DONE, task.getStatus());
        assertEquals(TaskPriority.HIGH, task.getPriority());
        assertNull(task.getDescription());
        assertNull(task.getDueDate());
    }

    @Test
    void preservesStatusAndPriorityWhenTheyAreOmittedFromUpdate() {
        Task task = completeTask();
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated task");

        taskMapper.updateEntity(request, task);

        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(TaskPriority.LOW, task.getPriority());
    }

    @Test
    void mapsEntityToResponseWithoutUserOrDeletionMetadata() {
        Task task = completeTask();

        TaskResponse response = taskMapper.toResponse(task);

        assertEquals(task.getId(), response.getId());
        assertEquals(task.getTitle(), response.getTitle());
        assertEquals(task.getDescription(), response.getDescription());
        assertEquals(task.getStatus(), response.getStatus());
        assertEquals(task.getPriority(), response.getPriority());
        assertEquals(task.getDueDate(), response.getDueDate());
        assertEquals(task.getCreatedAt(), response.getCreatedAt());
        assertEquals(task.getUpdatedAt(), response.getUpdatedAt());
        assertFalse(hasField(TaskResponse.class, "user"));
        assertFalse(hasField(TaskResponse.class, "deletedAt"));
    }

    private Task completeTask() {
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("Original task");
        task.setDescription("Description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(TaskPriority.LOW);
        task.setDueDate(LocalDate.of(2026, 9, 25));
        task.setCreatedAt(LocalDateTime.of(2026, 9, 17, 10, 0));
        task.setUpdatedAt(LocalDateTime.of(2026, 9, 17, 11, 0));
        task.setDeletedAt(LocalDateTime.of(2026, 9, 17, 12, 0));
        task.setUser(new User());
        return task;
    }

    private boolean hasField(Class<?> type, String name) {
        try {
            type.getDeclaredField(name);
            return true;
        } catch (NoSuchFieldException exception) {
            return false;
        }
    }
}
