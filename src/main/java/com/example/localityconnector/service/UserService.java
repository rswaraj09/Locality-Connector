package com.example.localityconnector.service;

import com.example.localityconnector.dto.UserSignupRequest;
import com.example.localityconnector.model.User;
import com.example.localityconnector.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public User signup(UserSignupRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // In production, this should be encrypted
        user.setAddress(request.getAddress());
        user.setPhoneNumber(request.getPhoneNumber());
        
        // Set timestamps
        user.prePersist();
        
        try {
            return userRepository.save(user);
        } catch (DuplicateKeyException ex) {
            // Handle race condition against unique index on email
            throw new RuntimeException("Email already exists");
        }
    }
    
    public Optional<User> login(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
    
    public User updateUser(String id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setName(userDetails.getName());
        user.setAddress(userDetails.getAddress());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
























