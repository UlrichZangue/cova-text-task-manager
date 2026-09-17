package com.taskmanager.backend.task.repository;

import com.taskmanager.backend.task.dto.TaskStatsResponse;
import com.taskmanager.backend.task.entity.Task;
import com.taskmanager.backend.task.entity.TaskStatus;
import com.taskmanager.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends
        JpaRepository<Task, UUID>,
        JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndUserAndDeletedAtIsNull(
            UUID id,
            User user
    );

    @Query("""
            select new com.taskmanager.backend.task.dto.TaskStatsResponse(
                count(task),
                sum(case when task.status = :todo then 1L else 0L end),
                sum(case when task.status = :inProgress then 1L else 0L end),
                sum(case when task.status = :done then 1L else 0L end)
            )
            from Task task
            where task.user = :user
              and task.deletedAt is null
            """)
    TaskStatsResponse findStatsByUser(
            @Param("user") User user,
            @Param("todo") TaskStatus todo,
            @Param("inProgress") TaskStatus inProgress,
            @Param("done") TaskStatus done
    );
}
