package com.taskmanagement.Controller;

import tools.jackson.databind.ObjectMapper;
import com.taskmanagement.DTO.TaskDTO;
import com.taskmanagement.Exception.TaskNotFoundException;
import com.taskmanagement.Model.TaskStatus;
import com.taskmanagement.Service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private ObjectMapper objectMapper;
    private TaskDTO sampleDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        sampleDTO = new TaskDTO();
        sampleDTO.setId("task-123");
        sampleDTO.setTitle("Sample Task");
        sampleDTO.setDescription("Sample Description");
        sampleDTO.setStatus(TaskStatus.PENDING);
        sampleDTO.setDueDate(LocalDate.now().plusDays(5));
    }

    // --- POST /tasks ---

    @Test
    void createTask_validRequest_shouldReturn201() throws Exception {
        when(taskService.createTask(any(TaskDTO.class))).thenReturn(sampleDTO);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("task-123"))
                .andExpect(jsonPath("$.title").value("Sample Task"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(taskService).createTask(any(TaskDTO.class));
    }

    @Test
    void createTask_blankTitle_shouldReturn400() throws Exception {
        TaskDTO invalid = new TaskDTO();
        invalid.setTitle("");
        invalid.setStatus(TaskStatus.PENDING);
        invalid.setDueDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_nullStatus_shouldReturn400() throws Exception {
        TaskDTO invalid = new TaskDTO();
        invalid.setTitle("Valid Title");
        invalid.setStatus(null);
        invalid.setDueDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_pastDueDate_shouldReturn400() throws Exception {
        TaskDTO invalid = new TaskDTO();
        invalid.setTitle("Valid Title");
        invalid.setStatus(TaskStatus.PENDING);
        invalid.setDueDate(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_nullDueDate_shouldReturn400() throws Exception {
        TaskDTO invalid = new TaskDTO();
        invalid.setTitle("Valid Title");
        invalid.setStatus(TaskStatus.PENDING);
        invalid.setDueDate(null);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // --- GET /tasks/{id} ---

    @Test
    void getTask_existingId_shouldReturn200() throws Exception {
        when(taskService.getTask("task-123")).thenReturn(sampleDTO);

        mockMvc.perform(get("/tasks/task-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("task-123"))
                .andExpect(jsonPath("$.title").value("Sample Task"));
    }

    @Test
    void getTask_nonExistingId_shouldReturn404() throws Exception {
        when(taskService.getTask("non-existent")).thenThrow(new TaskNotFoundException("non-existent"));

        mockMvc.perform(get("/tasks/non-existent"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Task not found with id: non-existent"));
    }

    // --- PUT /tasks/{id} ---

    @Test
    void updateTask_validRequest_shouldReturn200() throws Exception {
        TaskDTO updatedDTO = new TaskDTO();
        updatedDTO.setId("task-123");
        updatedDTO.setTitle("Updated Title");
        updatedDTO.setDescription("Updated Desc");
        updatedDTO.setStatus(TaskStatus.IN_PROGRESS);
        updatedDTO.setDueDate(LocalDate.now().plusDays(10));

        when(taskService.updateTask(eq("task-123"), any(TaskDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/tasks/task-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateTask_nonExistingId_shouldReturn404() throws Exception {
        when(taskService.updateTask(eq("non-existent"), any(TaskDTO.class)))
                .thenThrow(new TaskNotFoundException("non-existent"));

        mockMvc.perform(put("/tasks/non-existent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTask_invalidBody_shouldReturn400() throws Exception {
        TaskDTO invalid = new TaskDTO();
        invalid.setTitle("");
        invalid.setStatus(null);

        mockMvc.perform(put("/tasks/task-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // --- DELETE /tasks/{id} ---

    @Test
    void deleteTask_existingId_shouldReturn204() throws Exception {
        doNothing().when(taskService).deleteTask("task-123");

        mockMvc.perform(delete("/tasks/task-123"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask("task-123");
    }

    @Test
    void deleteTask_nonExistingId_shouldReturn404() throws Exception {
        doThrow(new TaskNotFoundException("non-existent")).when(taskService).deleteTask("non-existent");

        mockMvc.perform(delete("/tasks/non-existent"))
                .andExpect(status().isNotFound());
    }

    // --- GET /tasks ---

    @Test
    void getAllTasks_defaultParams_shouldReturn200() throws Exception {
        Page<TaskDTO> page = new PageImpl<>(List.of(sampleDTO), PageRequest.of(0, 20), 1);
        when(taskService.getAllTasks(eq(null), any())).thenReturn(page);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("task-123"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllTasks_withStatusFilter_shouldReturn200() throws Exception {
        Page<TaskDTO> page = new PageImpl<>(List.of(sampleDTO), PageRequest.of(0, 20), 1);
        when(taskService.getAllTasks(eq(TaskStatus.PENDING), any())).thenReturn(page);

        mockMvc.perform(get("/tasks").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void getAllTasks_withCustomPagination_shouldReturn200() throws Exception {
        Page<TaskDTO> page = new PageImpl<>(List.of(sampleDTO), PageRequest.of(1, 5), 6);
        when(taskService.getAllTasks(eq(null), any())).thenReturn(page);

        mockMvc.perform(get("/tasks")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllTasks_emptyResult_shouldReturn200WithEmptyContent() throws Exception {
        Page<TaskDTO> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(taskService.getAllTasks(eq(null), any())).thenReturn(emptyPage);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
