package com.taskmanagement.Config;

import com.taskmanagement.Model.Task;
import com.taskmanagement.Model.TaskStatus;
import com.taskmanagement.Repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

@Configuration
public class DummyDataConfig {
    @Bean
    public CommandLineRunner loadDummyData(
            TaskRepository taskRepository) {

        return args -> {

            if (taskRepository
                    .findAll(Pageable.unpaged())
                    .getTotalElements() > 0) {
                return;
            }

            taskRepository.save(createTask(
                    "Design login page",
                    "Create the user login screen",
                    "Prepare wireframes and review them with the UX team",
                    TaskStatus.PENDING,
                    LocalDate.now().plusDays(5)));

            taskRepository.save(createTask(
                    "Implement authentication",
                    "Add login and logout functionality",
                    "Implement authentication validation and error handling",
                    TaskStatus.IN_PROGRESS,
                    LocalDate.now().plusDays(10)));

            taskRepository.save(createTask(
                    "Create task API",
                    "Build REST APIs for task management",
                    "Implement create, read, update, and delete operations",
                    TaskStatus.PENDING,
                    LocalDate.now().minusDays(2)));

            taskRepository.save(createTask(
                    "Write unit tests",
                    "Add service layer unit tests",
                    "Cover success cases, validation failures, and missing task scenarios",
                    TaskStatus.DONE,
                    LocalDate.now().plusDays(14)));

            taskRepository.save(createTask(
                    "Update project documentation",
                    "Document the task management APIs",
                    "Add endpoint details, request examples, and response formats",
                    TaskStatus.IN_PROGRESS,
                    LocalDate.now().plusDays(7)));
        };
    }

    private Task createTask(
            String id,
            String title,
            String description,
            TaskStatus status,
            LocalDate dueDate) {

        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDueDate(dueDate);
        return task;
    }
}
