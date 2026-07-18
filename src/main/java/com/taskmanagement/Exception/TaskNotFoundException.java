package com.taskmanagement.Exception;

public class TaskNotFoundException extends RuntimeException{
    public TaskNotFoundException(String taskId) {
        super("Task not found with id: " + taskId);
    }
}
