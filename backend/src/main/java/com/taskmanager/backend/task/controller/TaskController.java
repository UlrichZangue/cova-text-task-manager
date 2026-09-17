package com.taskmanager.backend.task.controller;

import com.taskmanager.backend.task.dto.PageResponse;
import com.taskmanager.backend.task.dto.TaskRequest;
import com.taskmanager.backend.task.dto.TaskResponse;
import com.taskmanager.backend.task.dto.TaskStatsResponse;
import com.taskmanager.backend.task.entity.TaskPriority;
import com.taskmanager.backend.task.entity.TaskStatus;
import com.taskmanager.backend.task.service.TaskService;
import com.taskmanager.backend.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tasks", description = "Secure management of the authenticated user's tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a task")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created"),
            @ApiResponse(responseCode = "400", description = "Invalid task", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> create(
            @Valid @RequestBody TaskRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.create(request, authentication.getName()));
    }

    @GetMapping
    @Operation(
            summary = "Search and list tasks",
            description = "Returns only active tasks owned by the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated task results"),
            @ApiResponse(responseCode = "400", description = "Invalid filter, page or sort", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<TaskResponse>> findAll(
            @Parameter(description = "Case-insensitive text searched in title and description", example = "rapport")
            @RequestParam(required = false) String search,
            @Parameter(description = "Task status filter", example = "TODO")
            @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Task priority filter", example = "HIGH")
            @RequestParam(required = false) TaskPriority priority,
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size between 1 and 100", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(
                    description = "Sort field and direction. Allowed fields: id, title, status, priority, dueDate, createdAt, updatedAt",
                    example = "createdAt,desc"
            )
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Authentication authentication
    ) {
        return ResponseEntity.ok(taskService.findAll(
                authentication.getName(),
                search,
                status,
                priority,
                page,
                size,
                sort
        ));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard task statistics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Counts of active tasks by status"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskStatsResponse> getStats(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                taskService.getStats(authentication.getName())
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task not found or not owned by the user", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> findById(
            @Parameter(description = "Task UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                taskService.findById(id, authentication.getName())
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated"),
            @ApiResponse(responseCode = "400", description = "Invalid task", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task not found or not owned by the user", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> update(
            @Parameter(description = "Task UUID") @PathVariable UUID id,
            @Valid @RequestBody TaskRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                taskService.update(id, request, authentication.getName())
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a task",
            description = "Soft-deletes a task owned by the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task not found or not owned by the user", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Task UUID") @PathVariable UUID id,
            Authentication authentication
    ) {
        taskService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
