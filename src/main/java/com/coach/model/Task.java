package com.coach.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    private int id;
    private int userId;
    private String title;
    private String description;
    private String category;
    private int priority; // 1 = High, 2 = Medium, 3 = Low
    private LocalDate deadline;
    private String status; // TODO, IN_PROGRESS, DONE
    private int timeSpentMinutes;
    private int estimatedTimeMinutes;
    private LocalDateTime createdAt;

    public Task() {
    }

    public Task(int id, int userId, String title, String description, String category, int priority, LocalDate deadline, String status, int timeSpentMinutes, int estimatedTimeMinutes, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.deadline = deadline;
        this.status = status;
        this.timeSpentMinutes = timeSpentMinutes;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTimeSpentMinutes() { return timeSpentMinutes; }
    public void setTimeSpentMinutes(int timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; }

    public int getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public void setEstimatedTimeMinutes(int estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
