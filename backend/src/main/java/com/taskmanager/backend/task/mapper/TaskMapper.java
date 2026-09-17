package com.taskmanager.backend.task.mapper;

import com.taskmanager.backend.task.dto.TaskRequest;
import com.taskmanager.backend.task.dto.TaskResponse;
import com.taskmanager.backend.task.entity.Task;
import com.taskmanager.backend.task.entity.TaskPriority;
import com.taskmanager.backend.task.entity.TaskStatus;
import com.taskmanager.backend.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public Task toEntity(TaskRequest request, User user) {
        Task task = new Task();
        task.setUser(user);
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setStatus(defaultStatus(request.getStatus()));
        task.setPriority(defaultPriority(request.getPriority()));
        task.setDueDate(request.getDueDate());
        return task;
    }

    public void updateEntity(TaskRequest request, Task task) {
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
    }

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private TaskStatus defaultStatus(TaskStatus status) {
        return status == null ? TaskStatus.TODO : status;
    }

    private TaskPriority defaultPriority(TaskPriority priority) {
        return priority == null ? TaskPriority.MEDIUM : priority;
    }
}
