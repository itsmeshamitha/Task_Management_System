package com.taskmanagement.Model;

import com.taskmanagement.DTO.TaskDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TaskDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void allArgsConstructor_shouldSetAllFields() {
        TaskDTO dto = new TaskDTO("id-1", "Title", "Desc", TaskStatus.PENDING, LocalDate.of(2027, 6, 1));

        assertEquals("id-1", dto.getId());
        assertEquals("Title", dto.getTitle());
        assertEquals("Desc", dto.getDescription());
        assertEquals(TaskStatus.PENDING, dto.getStatus());
        assertEquals(LocalDate.of(2027, 6, 1), dto.getDueDate());
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        TaskDTO dto = new TaskDTO();
        assertNull(dto.getId());
        assertNull(dto.getTitle());
        assertNull(dto.getDescription());
        assertNull(dto.getStatus());
        assertNull(dto.getDueDate());
    }

    @Test
    void settersAndGetters_shouldWork() {
        TaskDTO dto = new TaskDTO();
        dto.setId("id-2");
        dto.setTitle("My Task");
        dto.setDescription("My Desc");
        dto.setStatus(TaskStatus.IN_PROGRESS);
        dto.setDueDate(LocalDate.of(2027, 12, 25));

        assertEquals("id-2", dto.getId());
        assertEquals("My Task", dto.getTitle());
        assertEquals("My Desc", dto.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, dto.getStatus());
        assertEquals(LocalDate.of(2027, 12, 25), dto.getDueDate());
    }

    @Test
    void validation_titleBlank_shouldFail() {
        TaskDTO dto = new TaskDTO(null, "", "Desc", TaskStatus.PENDING, LocalDate.now().plusDays(5));

        Set<ConstraintViolation<TaskDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Title is required")));
    }

    @Test
    void validation_titleTooLong_shouldFail() {
        TaskDTO dto = new TaskDTO(null, "A".repeat(151), "Desc", TaskStatus.PENDING, LocalDate.now().plusDays(5));

        Set<ConstraintViolation<TaskDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Title cannot exceed 150 characters")));
    }

    @Test
    void validation_statusNull_shouldFail() {
        TaskDTO dto = new TaskDTO(null, "Title", "Desc", null, LocalDate.now().plusDays(5));

        Set<ConstraintViolation<TaskDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Status is required")));
    }

    @Test
    void validation_dueDateNull_shouldFail() {
        TaskDTO dto = new TaskDTO(null, "Title", "Desc", TaskStatus.PENDING, null);

        Set<ConstraintViolation<TaskDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Due date is required")));
    }

    @Test
    void validation_dueDateInPast_shouldFail() {
        TaskDTO dto = new TaskDTO(null, "Title", "Desc", TaskStatus.PENDING, LocalDate.now().minusDays(1));

        Set<ConstraintViolation<TaskDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Due date must be a valid date in the future")));
    }

    @Test
    void validation_validDTO_shouldPass() {
        TaskDTO dto = new TaskDTO(null, "Valid Title", "Valid Desc", TaskStatus.DONE, LocalDate.now().plusDays(10));

        Set<ConstraintViolation<TaskDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validation_descriptionTooLong_shouldFail() {
        TaskDTO dto = new TaskDTO(null, "Title", "A".repeat(2001), TaskStatus.PENDING, LocalDate.now().plusDays(5));

        Set<ConstraintViolation<TaskDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Description cannot exceed 2000 characters")));
    }
}
