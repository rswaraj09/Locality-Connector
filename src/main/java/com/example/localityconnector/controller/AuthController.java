package com.example.localityconnector.controller;

import com.example.localityconnector.dto.BusinessSignupRequest;
import com.example.localityconnector.dto.UserSignupRequest;
import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.User;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final BusinessService businessService;
    
    // User Signup
    @PostMapping("/user/signup")
    public ResponseEntity<?> userSignup(@Valid @RequestBody UserSignupRequest request) {
        try {
            User user = userService.signup(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Business Signup
    @PostMapping("/business/signup")
    public ResponseEntity<?> businessSignup(@Valid @RequestBody BusinessSignupRequest request) {
        try {
            Business business = businessService.signup(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Business registered successfully");
            response.put("businessId", business.getId());
            response.put("businessName", business.getBusinessName());
            response.put("email", business.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // User Login
    @PostMapping("/user/login")
    public ResponseEntity<?> userLogin(@RequestParam String email, @RequestParam String password) {
        try {
            var user = userService.login(email, password);
            if (user.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("userId", user.get().getId());
                response.put("name", user.get().getName());
                response.put("email", user.get().getEmail());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid email or password");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Business Login
    @PostMapping("/business/login")
    public ResponseEntity<?> businessLogin(@RequestParam String email, @RequestParam String password, HttpSession session) {
        try {
            var business = businessService.login(email, password);
            if (business.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("businessId", business.get().getId());
                response.put("businessName", business.get().getBusinessName());
                response.put("email", business.get().getEmail());
                response.put("category", business.get().getCategory());
                // store in session for later use in pages
                session.setAttribute("loggedInBusinessName", business.get().getBusinessName());
                session.setAttribute("loggedInBusinessId", business.get().getId());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid email or password");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Get all users (for admin purposes)
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            var users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch users: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Get all businesses (for admin purposes)
    @GetMapping("/businesses")
    public ResponseEntity<?> getAllBusinesses() {
        try {
            var businesses = businessService.getAllBusinesses();
            return ResponseEntity.ok(businesses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch businesses: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Session helper: returns the logged-in business name, if set
    @GetMapping("/session/business-name")
    public ResponseEntity<?> getSessionBusinessName(HttpSession session) {
        Object n = session.getAttribute("loggedInBusinessName");
        if (n == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(java.util.Map.of("businessName", n.toString()));
    }
}















