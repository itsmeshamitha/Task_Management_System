package com.taskmanagement.Controller;


import com.taskmanagement.DTO.TaskDTO;
import com.taskmanagement.Exception.TaskNotFoundException;
import com.taskmanagement.Model.TaskStatus;
import com.taskmanagement.Service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {


    private final TaskService taskManagementService;

    public TaskController(TaskService taskManagementService) {
        this.taskManagementService = taskManagementService;
    }

    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskDTO taskDto) {
        try {
            TaskDTO createdTask = taskManagementService.createTask(taskDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getTask(@PathVariable String id) {
        try {
            TaskDTO taskDto = taskManagementService.getTask(id);
            return ResponseEntity.ok(taskDto);

        } catch (TaskNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable String id, @Valid @RequestBody TaskDTO  taskDto) {
        try {
            TaskDTO updatedTask = taskManagementService.updateTask(id, taskDto);
            return ResponseEntity.ok(updatedTask);

        } catch (TaskNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable String id) {
        try {
            taskManagementService.deleteTask(id);
            return ResponseEntity.noContent().build();

        } catch (TaskNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());

            Page<TaskDTO> tasks = taskManagementService.getAllTasks(status, pageable);
            return ResponseEntity.ok(tasks);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

