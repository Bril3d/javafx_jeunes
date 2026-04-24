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
}
