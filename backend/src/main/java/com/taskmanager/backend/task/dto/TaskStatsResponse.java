package com.taskmanager.backend.task.dto;

public class TaskStatsResponse {

    private final long total;
    private final long todo;
    private final long inProgress;
    private final long done;

    public TaskStatsResponse(
            Long total,
            Long todo,
            Long inProgress,
            Long done
    ) {
        this.total = valueOrZero(total);
        this.todo = valueOrZero(todo);
        this.inProgress = valueOrZero(inProgress);
        this.done = valueOrZero(done);
    }

    public long getTotal() {
        return total;
    }

    public long getTodo() {
        return todo;
    }

    public long getInProgress() {
        return inProgress;
    }

    public long getDone() {
        return done;
    }

    private static long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }
}
