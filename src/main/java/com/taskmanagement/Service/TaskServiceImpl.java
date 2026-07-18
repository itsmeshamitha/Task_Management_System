package com.taskmanagement.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import com.taskmanagement.DTO.TaskDTO;
import com.taskmanagement.Exception.TaskNotFoundException;
import com.taskmanagement.Model.Task;
import com.taskmanagement.Model.TaskStatus;
import com.taskmanagement.Repository.TaskRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl implements TaskService {


    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public TaskDTO createTask(TaskDTO taskDto) {
        Task task = new Task();
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setStatus(taskDto.getStatus());
        task.setDueDate(taskDto.getDueDate());

        Task savedTask = taskRepository.save(task);
        return convertToDto(savedTask);
    }

    @Override
    public TaskDTO getTask(String id) {
        Task task = (Task) taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        return convertToDto(task);
    }

    @Override
    public TaskDTO updateTask(String id, TaskDTO taskDto) {
        Task existingTask = (Task) taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException(id));

        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setStatus(taskDto.getStatus());
        existingTask.setDueDate(taskDto.getDueDate());

        Task updatedTask = taskRepository.save(existingTask);
        return convertToDto(updatedTask);

    }

    @Override
    public void deleteTask(String id) {
        Task task = (Task) taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException(id));

        taskRepository.delete(task);
    }

    @Override
    public Page<TaskDTO> getAllTasks(TaskStatus status, Pageable pageable) {
        Page<Task> taskPage;

        if (status != null) {
            taskPage = taskRepository.findByStatus(status, pageable);
        } else {
            taskPage = taskRepository.findAll(pageable);
        }

        return taskPage.map(this::convertToDto);
    }

    private TaskDTO convertToDto(Task task) {
        TaskDTO taskDto = new TaskDTO();

        taskDto.setId(task.getId());
        taskDto.setTitle(task.getTitle());
        taskDto.setDescription(task.getDescription());
        taskDto.setStatus(task.getStatus());
        taskDto.setDueDate(task.getDueDate());
        return taskDto;
    }
}
