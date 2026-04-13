package com.coach.service;

import com.coach.model.Task;
import com.coach.model.User;
import com.coach.repository.TaskRepository;

import java.util.List;

public class TaskService {
    private final TaskRepository taskRepository;
    private final UserService userService;

    public TaskService(UserService userService) {
        this.taskRepository = new TaskRepository();
        this.userService = userService;
    }

    public Task addTask(Task task) {
        User user = userService.getCurrentUser();
        if (user != null) {
            task.setUserId(user.getId());
            return taskRepository.save(task);
        }
        return null;
    }

    public List<Task> bulkAddTasks(List<Task> tasks) {
        User user = userService.getCurrentUser();
        List<Task> savedTasks = new java.util.ArrayList<>();
        if (user != null) {
            for (Task t : tasks) {
                t.setUserId(user.getId());
                Task saved = taskRepository.save(t);
                if (saved != null) savedTasks.add(saved);
            }
        }
        return savedTasks;
    }

    public List<Task> getMyTasks() {
        User user = userService.getCurrentUser();
        if (user != null) {
            return taskRepository.findByUserId(user.getId());
        }
        return List.of();
    }

    public boolean updateTask(Task task) {
        return taskRepository.update(task);
    }

    public boolean deleteTask(int taskId) {
        User user = userService.getCurrentUser();
        if (user != null) {
            return taskRepository.delete(taskId, user.getId());
        }
        return false;
    }

    // Productivity metrics
    public double getCompletionRate() {
        List<Task> tasks = getMyTasks();
        if (tasks.isEmpty()) return 0.0;
        
        long completed = tasks.stream()
            .filter(t -> "DONE".equalsIgnoreCase(t.getStatus()))
            .count();
        return (double) completed / tasks.size();
    }
}
