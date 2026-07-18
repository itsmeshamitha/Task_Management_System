package com.taskmanagement.Service;


import com.taskmanagement.DTO.TaskDTO;
import com.taskmanagement.Model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface TaskService {
    TaskDTO createTask(TaskDTO taskDto);

    TaskDTO getTask(String id);

    TaskDTO updateTask(String id, TaskDTO taskDto);

    void deleteTask(String id);

    Page<TaskDTO> getAllTasks(TaskStatus status, Pageable pageable);


}
