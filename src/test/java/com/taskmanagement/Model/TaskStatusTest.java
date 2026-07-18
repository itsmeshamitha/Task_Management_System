package com.taskmanagement.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskStatusTest {

    @Test
    void enum_shouldHaveThreeValues() {
        assertEquals(3, TaskStatus.values().length);
    }

    @Test
    void enum_shouldContainExpectedValues() {
        assertNotNull(TaskStatus.valueOf("PENDING"));
        assertNotNull(TaskStatus.valueOf("IN_PROGRESS"));
        assertNotNull(TaskStatus.valueOf("DONE"));
    }

    @Test
    void valueOf_invalidValue_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> TaskStatus.valueOf("INVALID"));
    }
}
