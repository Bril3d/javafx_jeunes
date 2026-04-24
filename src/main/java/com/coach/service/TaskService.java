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

    public List<Task> getAutoPrioritizedTasks() {
        List<Task> tasks = new java.util.ArrayList<>(getMyTasks());
        tasks.sort((t1, t2) -> {
            // DONE tasks go to bottom
            if (t1.getStatus().equals("DONE") && !t2.getStatus().equals("DONE")) return 1;
            if (!t1.getStatus().equals("DONE") && t2.getStatus().equals("DONE")) return -1;
            
            // Priority score: Priority(1-3) * 10 + DaysUntilDeadline
            long score1 = calculateScore(t1);
            long score2 = calculateScore(t2);
            return Long.compare(score1, score2);
        });
        return tasks;
    }

    private long calculateScore(Task t) {
        long score = t.getPriority() * 100; // Priority is 1, 2, or 3. 1 is highest.
        if (t.getDeadline() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), t.getDeadline());
            score += Math.max(0, days);
        } else {
            score += 1000; // No deadline = lower priority
        }
        return score;
    }
}
