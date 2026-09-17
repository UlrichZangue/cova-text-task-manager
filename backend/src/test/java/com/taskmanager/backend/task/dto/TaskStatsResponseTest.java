package com.taskmanager.backend.task.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskStatsResponseTest {

    @Test
    void normalizesEmptyAggregateValuesToZero() {
        TaskStatsResponse response = new TaskStatsResponse(0L, null, null, null);

        assertEquals(0, response.getTotal());
        assertEquals(0, response.getTodo());
        assertEquals(0, response.getInProgress());
        assertEquals(0, response.getDone());
    }
}
