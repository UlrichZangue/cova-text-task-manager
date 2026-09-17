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
import com.taskmanager.backend.task.repository.TaskSpecifications;
import com.taskmanager.backend.user.entity.User;
import com.taskmanager.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "title",
            "status",
            "priority",
            "dueDate",
            "createdAt",
            "updatedAt"
    );

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository,
            TaskMapper taskMapper
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    @Transactional
    public TaskResponse create(TaskRequest request, String authenticatedEmail) {
        User user = getAuthenticatedUser(authenticatedEmail);
        Task task = taskMapper.toEntity(request, user);
        return taskMapper.toResponse(taskRepository.saveAndFlush(task));
    }

    public PageResponse<TaskResponse> findAll(
            String authenticatedEmail,
            String search,
            TaskStatus status,
            TaskPriority priority,
            int page,
            int size,
            String sort
    ) {
        User user = getAuthenticatedUser(authenticatedEmail);
        PageRequest pageRequest = PageRequest.of(
                validatePage(page),
                validateSize(size),
                parseSort(sort)
        );
        Page<Task> tasks = taskRepository.findAll(
                TaskSpecifications.filteredFor(user, search, status, priority),
                pageRequest
        );
        return PageResponse.from(tasks, taskMapper::toResponse);
    }

    public TaskResponse findById(UUID id, String authenticatedEmail) {
        User user = getAuthenticatedUser(authenticatedEmail);
        return taskMapper.toResponse(findOwnedTask(id, user));
    }

    public TaskStatsResponse getStats(String authenticatedEmail) {
        User user = getAuthenticatedUser(authenticatedEmail);
        return taskRepository.findStatsByUser(
                user,
                TaskStatus.TODO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.DONE
        );
    }

    @Transactional
    public TaskResponse update(
            UUID id,
            TaskRequest request,
            String authenticatedEmail
    ) {
        User user = getAuthenticatedUser(authenticatedEmail);
        Task task = findOwnedTask(id, user);
        taskMapper.updateEntity(request, task);
        return taskMapper.toResponse(taskRepository.saveAndFlush(task));
    }

    @Transactional
    public void delete(UUID id, String authenticatedEmail) {
        User user = getAuthenticatedUser(authenticatedEmail);
        Task task = findOwnedTask(id, user);
        task.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(
                        "Authenticated user no longer exists"
                ));
    }

    private Task findOwnedTask(UUID id, User user) {
        return taskRepository.findByIdAndUserAndDeletedAtIsNull(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private int validatePage(int page) {
        if (page < 0) {
            throw new BadRequestException("Page index must be zero or greater");
        }
        return page;
    }

    private int validateSize(int size) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }
        return size;
    }

    private Sort parseSort(String sort) {
        String[] parts = sort == null ? new String[0] : sort.split(",", -1);
        if (parts.length != 2 || !ALLOWED_SORT_FIELDS.contains(parts[0])) {
            throw new BadRequestException("Invalid task sort");
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(parts[1].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Sort direction must be asc or desc");
        }

        Sort result = Sort.by(direction, parts[0]);
        return "id".equals(parts[0])
                ? result
                : result.and(Sort.by(Sort.Direction.ASC, "id"));
    }
}
