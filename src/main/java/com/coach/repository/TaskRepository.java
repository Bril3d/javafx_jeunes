package com.coach.repository;

import com.coach.config.DatabaseConfig;
import com.coach.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    public Task save(Task task) {
        String sql = "INSERT INTO tasks (user_id, title, description, category, priority, deadline, status, time_spent_minutes, estimated_time_minutes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setTaskParams(pstmt, task);
            
            if (pstmt.executeUpdate() > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        task.setId(rs.getInt(1));
                        return task;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean update(Task task) {
        String sql = "UPDATE tasks SET title=?, description=?, category=?, priority=?, deadline=?, status=?, time_spent_minutes=?, estimated_time_minutes=? WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getCategory());
            pstmt.setInt(4, task.getPriority());
            if (task.getDeadline() != null) {
                pstmt.setDate(5, Date.valueOf(task.getDeadline()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }
            pstmt.setString(6, task.getStatus());
            pstmt.setInt(7, task.getTimeSpentMinutes());
            pstmt.setInt(8, task.getEstimatedTimeMinutes());
            pstmt.setInt(9, task.getId());
            pstmt.setInt(10, task.getUserId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id, int userId) {
        String sql = "DELETE FROM tasks WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Task> findByUserId(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY deadline ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(extractTask(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    private void setTaskParams(PreparedStatement pstmt, Task task) throws SQLException {
        pstmt.setInt(1, task.getUserId());
        pstmt.setString(2, task.getTitle());
        pstmt.setString(3, task.getDescription());
        pstmt.setString(4, task.getCategory());
        pstmt.setInt(5, task.getPriority());
        if (task.getDeadline() != null) {
            pstmt.setDate(6, Date.valueOf(task.getDeadline()));
        } else {
            pstmt.setNull(6, Types.DATE);
        }
        pstmt.setString(7, task.getStatus());
        pstmt.setInt(8, task.getTimeSpentMinutes());
        pstmt.setInt(9, task.getEstimatedTimeMinutes());
    }

    private Task extractTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setUserId(rs.getInt("user_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setCategory(rs.getString("category"));
        task.setPriority(rs.getInt("priority"));
        Date deadline = rs.getDate("deadline");
        if (deadline != null) {
            task.setDeadline(deadline.toLocalDate());
        }
        task.setStatus(rs.getString("status"));
        task.setTimeSpentMinutes(rs.getInt("time_spent_minutes"));
        task.setEstimatedTimeMinutes(rs.getInt("estimated_time_minutes"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            task.setCreatedAt(createdAt.toLocalDateTime());
        }
        return task;
    }

    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(extractTask(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
}
