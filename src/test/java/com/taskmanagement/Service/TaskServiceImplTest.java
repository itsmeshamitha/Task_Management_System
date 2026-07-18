package com.taskmanagement.Service;

import com.taskmanagement.DTO.TaskDTO;
import com.taskmanagement.Exception.TaskNotFoundException;
import com.taskmanagement.Model.Task;
import com.taskmanagement.Model.TaskStatus;
import com.taskmanagement.Repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task sampleTask;
    private TaskDTO sampleTaskDTO;

    @BeforeEach
    void setUp() {
        sampleTask = new Task();
        sampleTask.setId("task-123");
        sampleTask.setTitle("Sample Task");
        sampleTask.setDescription("Sample Description");
        sampleTask.setStatus(TaskStatus.PENDING);
        sampleTask.setDueDate(LocalDate.now().plusDays(5));

        sampleTaskDTO = new TaskDTO();
        sampleTaskDTO.setTitle("Sample Task");
        sampleTaskDTO.setDescription("Sample Description");
        sampleTaskDTO.setStatus(TaskStatus.PENDING);
        sampleTaskDTO.setDueDate(LocalDate.now().plusDays(5));
    }

    // --- createTask ---

    @Test
    void createTask_validDTO_shouldReturnCreatedTaskDTO() {
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskDTO result = taskService.createTask(sampleTaskDTO);

        assertNotNull(result);
        assertEquals("task-123", result.getId());
        assertEquals("Sample Task", result.getTitle());
        assertEquals("Sample Description", result.getDescription());
        assertEquals(TaskStatus.PENDING, result.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // --- getTask ---

    @Test
    void getTask_existingId_shouldReturnTaskDTO() {
        when(taskRepository.findById("task-123")).thenReturn(Optional.of(sampleTask));

        TaskDTO result = taskService.getTask("task-123");

        assertNotNull(result);
        assertEquals("task-123", result.getId());
        assertEquals("Sample Task", result.getTitle());
        verify(taskRepository, times(1)).findById("task-123");
    }

    @Test
    void getTask_nonExistingId_shouldThrowTaskNotFoundException() {
        when(taskRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTask("non-existent"));
        verify(taskRepository, times(1)).findById("non-existent");
    }

    // --- updateTask ---

    @Test
    void updateTask_existingId_shouldReturnUpdatedTaskDTO() {
        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setDescription("Updated Desc");
        updateDTO.setStatus(TaskStatus.IN_PROGRESS);
        updateDTO.setDueDate(LocalDate.now().plusDays(10));

        Task updatedTask = new Task();
        updatedTask.setId("task-123");
        updatedTask.setTitle("Updated Title");
        updatedTask.setDescription("Updated Desc");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        updatedTask.setDueDate(LocalDate.now().plusDays(10));

        when(taskRepository.findById("task-123")).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        TaskDTO result = taskService.updateTask("task-123", updateDTO);

        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Desc", result.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        verify(taskRepository).findById("task-123");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_nonExistingId_shouldThrowTaskNotFoundException() {
        when(taskRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> taskService.updateTask("non-existent", sampleTaskDTO));
        verify(taskRepository).findById("non-existent");
        verify(taskRepository, never()).save(any(Task.class));
    }

    // --- deleteTask ---

    @Test
    void deleteTask_existingId_shouldDeleteTask() {
        when(taskRepository.findById("task-123")).thenReturn(Optional.of(sampleTask));
        doNothing().when(taskRepository).delete(sampleTask);

        assertDoesNotThrow(() -> taskService.deleteTask("task-123"));
        verify(taskRepository).findById("task-123");
        verify(taskRepository).delete(sampleTask);
    }

    @Test
    void deleteTask_nonExistingId_shouldThrowTaskNotFoundException() {
        when(taskRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask("non-existent"));
        verify(taskRepository).findById("non-existent");
        verify(taskRepository, never()).delete(any(Task.class));
    }

    // --- getAllTasks ---

    @Test
    void getAllTasks_noStatusFilter_shouldReturnAllTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(sampleTask), pageable, 1);
        when(taskRepository.findAll(pageable)).thenReturn(taskPage);

        Page<TaskDTO> result = taskService.getAllTasks(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Sample Task", result.getContent().get(0).getTitle());
        verify(taskRepository).findAll(pageable);
        verify(taskRepository, never()).findByStatus(any(), any());
    }

    @Test
    void getAllTasks_withStatusFilter_shouldReturnFilteredTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(sampleTask), pageable, 1);
        when(taskRepository.findByStatus(TaskStatus.PENDING, pageable)).thenReturn(taskPage);

        Page<TaskDTO> result = taskService.getAllTasks(TaskStatus.PENDING, pageable);

        assertEquals(1, result.getTotalElements());
        verify(taskRepository).findByStatus(TaskStatus.PENDING, pageable);
        verify(taskRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getAllTasks_emptyResult_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(taskRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<TaskDTO> result = taskService.getAllTasks(null, pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // --- DTO conversion ---

    @Test
    void createTask_shouldMapAllFieldsCorrectly() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId("generated-id");
            return saved;
        });

        TaskDTO result = taskService.createTask(sampleTaskDTO);

        assertEquals("generated-id", result.getId());
        assertEquals(sampleTaskDTO.getTitle(), result.getTitle());
        assertEquals(sampleTaskDTO.getDescription(), result.getDescription());
        assertEquals(sampleTaskDTO.getStatus(), result.getStatus());
        assertEquals(sampleTaskDTO.getDueDate(), result.getDueDate());
    }
}
