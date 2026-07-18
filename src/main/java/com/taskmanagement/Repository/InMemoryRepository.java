package com.taskmanagement.Repository;

import com.taskmanagement.Model.Task;
import com.taskmanagement.Model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRepository implements TaskRepository{

    private final Map<String, Task> taskStore = new ConcurrentHashMap<>();

    @Override
    public Page<Task> findByStatus(TaskStatus status, Pageable pageable) {
        List<Task> filtered = taskStore.values().stream()
                .filter(task -> Objects.equals(status, task.getStatus()))
                .toList();
        return createPage(filtered, pageable);
    }

    @Override
    public Page<Task> findAll(Pageable pageable) {
        return createPage(new ArrayList<>(taskStore.values()), pageable);
    }

    @Override
    public Optional<Task> findById(String id) {
        return Optional.ofNullable(taskStore.get(id));
    }

    @Override
    public Task save(Task task) {
        if (task.getId() == null || task.getId().isBlank()) {
            task.setId(UUID.randomUUID().toString());
        }
        taskStore.put(task.getId(), task);
        return task;
    }

    @Override
    public void delete(Task task) {
        if (task != null && task.getId() != null) {
            taskStore.remove(task.getId());
        }
    }

    private Page<Task> createPage(List<Task> tasks, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(tasks);
        }

        int start = (int) Math.min(pageable.getOffset(), tasks.size());
        int end = Math.min(start + pageable.getPageSize(), tasks.size());
        return new PageImpl<>(tasks.subList(start, end), pageable, tasks.size());
    }
}
