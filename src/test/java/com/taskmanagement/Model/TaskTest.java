package com.taskmanagement.Model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void constructor_shouldGenerateUniqueId() {
        Task task1 = new Task();
        Task task2 = new Task();

        assertNotNull(task1.getId());
        assertNotNull(task2.getId());
        assertNotEquals(task1.getId(), task2.getId());
    }

    @Test
    void gettersAndSetters_shouldWorkCorrectly() {
        Task task = new Task();
        task.setId("test-id");
        task.setTitle("Test Title");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDate.of(2027, 1, 1));

        assertEquals("test-id", task.getId());
        assertEquals("Test Title", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals(LocalDate.of(2027, 1, 1), task.getDueDate());
    }

    @Test
    void validation_titleBlank_shouldFail() {
        Task task = new Task();
        task.setTitle("");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDate.now().plusDays(5));

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Title is required")));
    }

    @Test
    void validation_titleTooLong_shouldFail() {
        Task task = new Task();
        task.setTitle("A".repeat(151));
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDate.now().plusDays(5));

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Title cannot exceed 150 characters")));
    }

    @Test
    void validation_descriptionTooLong_shouldFail() {
        Task task = new Task();
        task.setTitle("Valid Title");
        task.setDescription("A".repeat(2001));
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDate.now().plusDays(5));

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Description cannot exceed 2000 characters")));
    }

    @Test
    void validation_statusNull_shouldFail() {
        Task task = new Task();
        task.setTitle("Valid Title");
        task.setStatus(null);
        task.setDueDate(LocalDate.now().plusDays(5));

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Status is required")));
    }

    @Test
    void validation_dueDateInPast_shouldFail() {
        Task task = new Task();
        task.setTitle("Valid Title");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDate.now().minusDays(1));

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Due date must be a valid date in the future")));
    }

    @Test
    void validation_validTask_shouldPass() {
        Task task = new Task();
        task.setTitle("Valid Title");
        task.setDescription("Valid description");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDate.now().plusDays(5));

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertTrue(violations.isEmpty());
    }
}
