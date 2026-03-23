package com.demo.upi.controller;

import com.demo.upi.entity.User;
import com.demo.upi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.demo.upi.dto.UpdateUserRequest;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> userList = users.stream().map(user -> {
            Map<String, Object> u = new HashMap<>();
            u.put("id", user.getId());
            u.put("fullName", user.getFullName());
            u.put("email", user.getEmail());
            u.put("mobileNumber", user.getMobileNumber());
            u.put("upiId", user.getUpiId() != null ? user.getUpiId().getUpiId() : null);
            u.put("isBlocked", user.isBlocked());
            u.put("role", user.getRole().name());
            return u;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(userList);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok("User deleted");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
user.setBlocked(request.getIsBlocked());
        userRepository.save(user);
        System.out.println("AdminController updated user " + id + " blocked=" + user.isBlocked());

        Map<String, Object> u = new HashMap<>();
        u.put("id", user.getId());
        u.put("fullName", user.getFullName());
        u.put("email", user.getEmail());
        u.put("mobileNumber", user.getMobileNumber());
        u.put("upiId", user.getUpiId() != null ? user.getUpiId().getUpiId() : null);
        u.put("role", user.getRole().name());
        u.put("isBlocked", user.isBlocked());
        return ResponseEntity.ok(u);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalUsers = userRepository.count();
        long adminCount = userRepository.findByRole(User.Role.ADMIN).size();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("admins", adminCount);
        stats.put("regularUsers", totalUsers - adminCount);
        return ResponseEntity.ok(stats);
    }
}
