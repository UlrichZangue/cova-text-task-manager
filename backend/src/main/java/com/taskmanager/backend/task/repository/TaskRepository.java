package com.taskmanager.backend.task.repository;

import com.taskmanager.backend.task.entity.Task;
import com.taskmanager.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByUserAndDeletedAtIsNull(User user);

    Optional<Task> findByIdAndUserAndDeletedAtIsNull(
            UUID id,
            User user
    );
}