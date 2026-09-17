package com.taskmanager.backend.task.dto;

import com.taskmanager.backend.task.entity.TaskPriority;
import com.taskmanager.backend.task.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Fields used to create or replace a task")
public class TaskRequest {

    @Schema(example = "Prepare quarterly report", maxLength = 150)
    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @Schema(example = "Compile and review the finance figures", nullable = true)
    private String description;

    @Schema(example = "TODO", nullable = true)
    private TaskStatus status;

    @Schema(example = "HIGH", nullable = true)
    private TaskPriority priority;

    @Schema(example = "2026-10-01", nullable = true)
    private LocalDate dueDate;

    public TaskRequest() {
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
