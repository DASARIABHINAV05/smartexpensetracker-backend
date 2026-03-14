package com.expensetracker.controller;

import com.expensetracker.entity.User;
import com.expensetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000",
                         "https://smart-expense-tracker.onrender.com"})
public class UserController {

    @Autowired
    private UserService userService;

    // ── Register ──────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> req) {
        Map<String, Object> res = new HashMap<>();
        try {
            String username = req.get("username");
            String email    = req.get("email");
            String password = req.get("password");
            if (username == null || email == null || password == null) {
                res.put("error", "username, email and password are required.");
                return ResponseEntity.badRequest().body(res);
            }
            User user = userService.registerUser(username, email, password);
            res.put("message",  "User registered successfully");
            res.put("userId",   user.getId());
            res.put("username", user.getUsername());
            res.put("email",    user.getEmail());
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    // ── Login ─────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> req) {
        Map<String, Object> res = new HashMap<>();
        try {
            String username = req.get("username");
            String password = req.get("password");
            if (username == null || password == null) {
                res.put("error", "username and password are required.");
                return ResponseEntity.badRequest().body(res);
            }
            User user = userService.login(username, password);
            res.put("message",  "Login successful");
            res.put("userId",   user.getId());
            res.put("username", user.getUsername());
            res.put("email",    user.getEmail());
            res.put("phone",    user.getPhone() != null ? user.getPhone() : "");
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.status(401).body(res);
        }
    }

    // ── Get Profile ───────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        Map<String, Object> res = new HashMap<>();
        try {
            User user = userService.findById(id);
            res.put("userId",   user.getId());
            res.put("username", user.getUsername());
            res.put("email",    user.getEmail());
            res.put("phone",    user.getPhone() != null ? user.getPhone() : "");
            res.put("joinDate", user.getCreatedAt().toLocalDate().toString());
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.status(404).body(res);
        }
    }

    // ── Update Profile (username / email / phone) ─────────
    @PatchMapping("/{id}/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @PathVariable Long id,
            @RequestBody Map<String, String> req) {
        Map<String, Object> res = new HashMap<>();
        try {
            String username = req.get("username");
            String email    = req.get("email");
            String phone    = req.get("phone");
            if (username == null || email == null) {
                res.put("error", "username and email are required.");
                return ResponseEntity.badRequest().body(res);
            }
            User user = userService.updateProfile(id, username, email, phone);
            res.put("message",  "Profile updated successfully");
            res.put("userId",   user.getId());
            res.put("username", user.getUsername());
            res.put("email",    user.getEmail());
            res.put("phone",    user.getPhone() != null ? user.getPhone() : "");
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    // ── Change Password ───────────────────────────────────
    @PatchMapping("/{id}/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> req) {
        Map<String, Object> res = new HashMap<>();
        try {
            String current = req.get("currentPassword");
            String newPass = req.get("newPassword");
            if (current == null || newPass == null || newPass.length() < 6) {
                res.put("error", "currentPassword and newPassword (min 6 chars) are required.");
                return ResponseEntity.badRequest().body(res);
            }
            userService.changePassword(id, current, newPass);
            res.put("message", "Password changed successfully");
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
}
