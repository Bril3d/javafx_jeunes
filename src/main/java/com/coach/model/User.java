package com.coach.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private String goals;
    private String workRhythm;
    private String preferences;
    private String role;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(int id, String username, String email, String passwordHash) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = "USER";
    }

    public User(int id, String username, String email, String passwordHash, String goals, String workRhythm, String preferences, String role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.goals = goals;
        this.workRhythm = workRhythm;
        this.preferences = preferences;
        this.role = role;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }

    public String getWorkRhythm() { return workRhythm; }
    public void setWorkRhythm(String workRhythm) { this.workRhythm = workRhythm; }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
