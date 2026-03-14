package com.expensetracker.service;

import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username))
            throw new RuntimeException("Username '" + username + "' is already taken.");
        if (userRepository.existsByEmail(email))
            throw new RuntimeException("Email '" + email + "' is already registered.");

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    public User login(String username, String rawPassword) {
        User user = findByUsername(username);
        if (!passwordEncoder.matches(rawPassword, user.getPassword()))
            throw new RuntimeException("Invalid username or password.");
        return user;
    }

    public User updateProfile(Long id, String username, String email, String phone) {
        User user = findById(id);

        // Check username uniqueness only if changed
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username))
            throw new RuntimeException("Username '" + username + "' is already taken.");

        // Check email uniqueness only if changed
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email))
            throw new RuntimeException("Email '" + email + "' is already registered.");

        user.setUsername(username);
        user.setEmail(email);
        if (phone != null) user.setPhone(phone);
        return userRepository.save(user);
    }

    public User changePassword(Long id, String currentPassword, String newPassword) {
        User user = findById(id);
        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            throw new RuntimeException("Current password is incorrect.");
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
}
