package com.coach.service;

import com.coach.model.User;
import com.coach.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;
    private User currentUser;

    public UserService() {
        this.userRepository = new UserRepository();
        seedAdmin();
    }

    private void seedAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt(12));
            User admin = new User(0, "admin", "admin@productivitycoach.com", hashedPassword);
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("Admin account seeded: admin / admin123");
        }
    }

    public boolean register(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            return false; // User already exists
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        User newUser = new User(0, username, email, hashedPassword);
        
        User savedUser = userRepository.save(newUser);
        return savedUser != null;
    }

    public boolean login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                this.currentUser = user;
                return true;
            }
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean updateProfile(String goals, String workRhythm, String preferences) {
        if (currentUser == null) return false;
        currentUser.setGoals(goals);
        currentUser.setWorkRhythm(workRhythm);
        currentUser.setPreferences(preferences);
        return userRepository.updateProfile(currentUser);
    }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    public java.util.List<User> getAllUsers() {
        if (!isAdmin()) return new java.util.ArrayList<>();
        return userRepository.findAll();
    }

    public boolean deleteUser(int id) {
        if (!isAdmin()) return false;
        return userRepository.delete(id);
    }

    public boolean updateUser(User user) {
        if (!isAdmin()) return false;
        return userRepository.update(user);
    }
}
