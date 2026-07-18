package com.taskmanagement.Repository;

import com.taskmanagement.Model.Task;
import com.taskmanagement.Model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface TaskRepository{
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findAll(Pageable pageable);

    Optional<Task> findById(String id);

    Task save(Task task);

    void delete(Task task);
}
