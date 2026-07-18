package com.taskmanagement.Repository;

import com.taskmanagement.Model.Task;
import com.taskmanagement.Model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRepositoryTest {

    private InMemoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRepository();
    }

    private Task createTask(String title, TaskStatus status) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Description for " + title);
        task.setStatus(status);
        task.setDueDate(LocalDate.now().plusDays(5));
        return task;
    }

    // --- save ---

    @Test
    void save_newTask_shouldPersistAndReturnTask() {
        Task task = createTask("Task 1", TaskStatus.PENDING);
        Task saved = repository.save(task);

        assertNotNull(saved.getId());
        assertEquals("Task 1", saved.getTitle());
    }

    @Test
    void save_taskWithExistingId_shouldUpdate() {
        Task task = createTask("Task 1", TaskStatus.PENDING);
        Task saved = repository.save(task);
        String id = saved.getId();

        saved.setTitle("Updated Title");
        repository.save(saved);

        Optional<Task> found = repository.findById(id);
        assertTrue(found.isPresent());
        assertEquals("Updated Title", found.get().getTitle());
    }

    @Test
    void save_taskWithBlankId_shouldGenerateNewId() {
        Task task = createTask("Task 1", TaskStatus.PENDING);
        task.setId("");
        Task saved = repository.save(task);

        assertNotNull(saved.getId());
        assertFalse(saved.getId().isBlank());
    }

    @Test
    void save_taskWithNullId_shouldGenerateNewId() {
        Task task = createTask("Task 1", TaskStatus.PENDING);
        task.setId(null);
        Task saved = repository.save(task);

        assertNotNull(saved.getId());
    }

    // --- findById ---

    @Test
    void findById_existingTask_shouldReturnTask() {
        Task task = createTask("Task 1", TaskStatus.PENDING);
        Task saved = repository.save(task);

        Optional<Task> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_nonExistingTask_shouldReturnEmpty() {
        Optional<Task> found = repository.findById("non-existent-id");
        assertTrue(found.isEmpty());
    }

    // --- findAll ---

    @Test
    void findAll_emptyRepository_shouldReturnEmptyPage() {
        Page<Task> page = repository.findAll(PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void findAll_withTasks_shouldReturnAllTasks() {
        repository.save(createTask("Task 1", TaskStatus.PENDING));
        repository.save(createTask("Task 2", TaskStatus.DONE));
        repository.save(createTask("Task 3", TaskStatus.IN_PROGRESS));

        Page<Task> page = repository.findAll(PageRequest.of(0, 10));
        assertEquals(3, page.getTotalElements());
    }

    @Test
    void findAll_withPagination_shouldReturnCorrectPage() {
        for (int i = 1; i <= 5; i++) {
            repository.save(createTask("Task " + i, TaskStatus.PENDING));
        }

        Page<Task> page1 = repository.findAll(PageRequest.of(0, 2));
        assertEquals(2, page1.getContent().size());
        assertEquals(5, page1.getTotalElements());
        assertEquals(3, page1.getTotalPages());

        Page<Task> page3 = repository.findAll(PageRequest.of(2, 2));
        assertEquals(1, page3.getContent().size());
    }

    @Test
    void findAll_unpaged_shouldReturnAllTasks() {
        repository.save(createTask("Task 1", TaskStatus.PENDING));
        repository.save(createTask("Task 2", TaskStatus.DONE));

        Page<Task> page = repository.findAll(Pageable.unpaged());
        assertEquals(2, page.getTotalElements());
    }

    // --- findByStatus ---

    @Test
    void findByStatus_shouldReturnOnlyMatchingTasks() {
        repository.save(createTask("Task 1", TaskStatus.PENDING));
        repository.save(createTask("Task 2", TaskStatus.PENDING));
        repository.save(createTask("Task 3", TaskStatus.DONE));
        repository.save(createTask("Task 4", TaskStatus.IN_PROGRESS));

        Page<Task> pendingTasks = repository.findByStatus(TaskStatus.PENDING, PageRequest.of(0, 10));
        assertEquals(2, pendingTasks.getTotalElements());

        Page<Task> doneTasks = repository.findByStatus(TaskStatus.DONE, PageRequest.of(0, 10));
        assertEquals(1, doneTasks.getTotalElements());

        Page<Task> inProgressTasks = repository.findByStatus(TaskStatus.IN_PROGRESS, PageRequest.of(0, 10));
        assertEquals(1, inProgressTasks.getTotalElements());
    }

    @Test
    void findByStatus_noMatch_shouldReturnEmptyPage() {
        repository.save(createTask("Task 1", TaskStatus.PENDING));

        Page<Task> page = repository.findByStatus(TaskStatus.DONE, PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void findByStatus_withPagination_shouldPaginateFiltered() {
        for (int i = 1; i <= 5; i++) {
            repository.save(createTask("Task " + i, TaskStatus.PENDING));
        }
        repository.save(createTask("Done Task", TaskStatus.DONE));

        Page<Task> page = repository.findByStatus(TaskStatus.PENDING, PageRequest.of(0, 3));
        assertEquals(3, page.getContent().size());
        assertEquals(5, page.getTotalElements());
    }

    // --- delete ---

    @Test
    void delete_existingTask_shouldRemoveTask() {
        Task task = createTask("Task 1", TaskStatus.PENDING);
        Task saved = repository.save(task);

        repository.delete(saved);

        Optional<Task> found = repository.findById(saved.getId());
        assertTrue(found.isEmpty());
    }

    @Test
    void delete_nullTask_shouldNotThrow() {
        assertDoesNotThrow(() -> repository.delete(null));
    }

    @Test
    void delete_taskWithNullId_shouldNotThrow() {
        Task task = new Task();
        task.setId(null);
        assertDoesNotThrow(() -> repository.delete(task));
    }

    @Test
    void delete_nonExistingTask_shouldNotThrow() {
        Task task = createTask("Ghost", TaskStatus.PENDING);
        task.setId("non-existent");
        assertDoesNotThrow(() -> repository.delete(task));
    }

    // --- pagination edge cases ---

    @Test
    void findAll_pageOffsetBeyondTotal_shouldReturnEmptyContent() {
        repository.save(createTask("Task 1", TaskStatus.PENDING));

        Page<Task> page = repository.findAll(PageRequest.of(5, 10));
        assertTrue(page.getContent().isEmpty());
        assertEquals(1, page.getTotalElements());
    }
}
