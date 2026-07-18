package com.taskmanagement.Integration;

import tools.jackson.databind.ObjectMapper;
import com.taskmanagement.DTO.TaskDTO;
import com.taskmanagement.Model.TaskStatus;
import com.taskmanagement.Repository.InMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryRepository inMemoryRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private TaskDTO createValidDTO(String title, TaskStatus status) {
        TaskDTO dto = new TaskDTO();
        dto.setTitle(title);
        dto.setDescription("Description for " + title);
        dto.setStatus(status);
        dto.setDueDate(LocalDate.now().plusDays(7));
        return dto;
    }

    private String createTaskAndReturnId(String title, TaskStatus status) throws Exception {
        TaskDTO dto = createValidDTO(title, status);
        MvcResult result = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    // --- POST /tasks - Create ---

    @Test
    void createTask_shouldReturn201WithCreatedTask() throws Exception {
        TaskDTO dto = createValidDTO("Integration Test Task", TaskStatus.PENDING);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.description").value("Description for Integration Test Task"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.dueDate").isNotEmpty());
    }

    @Test
    void createTask_blankTitle_shouldReturn400() throws Exception {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("");
        dto.setStatus(TaskStatus.PENDING);
        dto.setDueDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_missingStatus_shouldReturn400() throws Exception {
        String json = """
                {"title": "Missing Status", "dueDate": "%s"}
                """.formatted(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_pastDueDate_shouldReturn400() throws Exception {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Past Due");
        dto.setStatus(TaskStatus.PENDING);
        dto.setDueDate(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // --- GET /tasks/{id} - Read ---

    @Test
    void getTask_existingTask_shouldReturn200() throws Exception {
        String id = createTaskAndReturnId("Get Test Task", TaskStatus.IN_PROGRESS);

        mockMvc.perform(get("/tasks/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Get Test Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void getTask_nonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/tasks/non-existent-id-12345"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Task not found with id: non-existent-id-12345")));
    }

    // --- PUT /tasks/{id} - Update ---

    @Test
    void updateTask_existingTask_shouldReturn200WithUpdatedData() throws Exception {
        String id = createTaskAndReturnId("Original Title", TaskStatus.PENDING);

        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setDescription("Updated Description");
        updateDTO.setStatus(TaskStatus.DONE);
        updateDTO.setDueDate(LocalDate.now().plusDays(14));

        mockMvc.perform(put("/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.status").value("DONE"));

        // Verify persistence
        mockMvc.perform(get("/tasks/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updateTask_nonExistingId_shouldReturn404() throws Exception {
        TaskDTO dto = createValidDTO("Update Non-existent", TaskStatus.PENDING);

        mockMvc.perform(put("/tasks/non-existent-id-99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTask_invalidBody_shouldReturn400() throws Exception {
        String id = createTaskAndReturnId("To Be Updated", TaskStatus.PENDING);

        TaskDTO invalid = new TaskDTO();
        invalid.setTitle("");
        invalid.setStatus(null);

        mockMvc.perform(put("/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // --- DELETE /tasks/{id} - Delete ---

    @Test
    void deleteTask_existingTask_shouldReturn204() throws Exception {
        String id = createTaskAndReturnId("To Be Deleted", TaskStatus.PENDING);

        mockMvc.perform(delete("/tasks/" + id))
                .andExpect(status().isNoContent());

        // Verify gone
        mockMvc.perform(get("/tasks/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_nonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/tasks/non-existent-id-delete"))
                .andExpect(status().isNotFound());
    }

    // --- GET /tasks - List ---

    @Test
    void getAllTasks_shouldReturnPagedResult() throws Exception {
        // DummyDataConfig seeds data, so tasks exist
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    void getAllTasks_withStatusFilter_shouldReturnFilteredResults() throws Exception {
        createTaskAndReturnId("Filtered Pending", TaskStatus.PENDING);

        mockMvc.perform(get("/tasks").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].status", everyItem(is("PENDING"))));
    }

    @Test
    void getAllTasks_withPagination_shouldRespectPageSize() throws Exception {
        mockMvc.perform(get("/tasks")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(lessThanOrEqualTo(2)))
                .andExpect(jsonPath("$.pageable.pageSize").value(2));
    }

    // --- End-to-end CRUD flow ---

    @Test
    void fullCrudFlow_shouldWorkEndToEnd() throws Exception {
        // Create
        TaskDTO createDTO = createValidDTO("CRUD Flow Task", TaskStatus.PENDING);
        MvcResult createResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // Read
        mockMvc.perform(get("/tasks/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("CRUD Flow Task"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Update
        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setTitle("CRUD Flow Updated");
        updateDTO.setDescription("Updated description");
        updateDTO.setStatus(TaskStatus.IN_PROGRESS);
        updateDTO.setDueDate(LocalDate.now().plusDays(20));

        mockMvc.perform(put("/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("CRUD Flow Updated"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // Delete
        mockMvc.perform(delete("/tasks/" + id))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/tasks/" + id))
                .andExpect(status().isNotFound());
    }
}
