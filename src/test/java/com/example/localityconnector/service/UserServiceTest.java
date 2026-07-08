package com.example.localityconnector.service;

import com.example.localityconnector.dto.UserSignupRequest;
import com.example.localityconnector.exception.DuplicateResourceException;
import com.example.localityconnector.exception.ResourceNotFoundException;
import com.example.localityconnector.model.User;
import com.example.localityconnector.repository.UserFirestoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserFirestoreRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserSignupRequest signupRequest;
    private User user;

    @BeforeEach
    void setUp() {
        signupRequest = new UserSignupRequest();
        signupRequest.setName("John Doe");
        signupRequest.setEmail("user@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setAddress("123 Main St");
        signupRequest.setPhoneNumber("1234567890");

        user = new User();
        user.setId("user-id");
        user.setName("John Doe");
        user.setEmail("user@example.com");
        user.setPassword("hashedPassword");
    }

    @Test
    void signup_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.signup(signupRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_DuplicateEmail_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            userService.signup(signupRequest);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        Optional<User> result = userService.login("user@example.com", "password123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("user@example.com", result.get().getEmail());
    }

    @Test
    void login_InvalidPassword_ReturnsEmpty() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act
        Optional<User> result = userService.login("user@example.com", "wrongpassword");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_Success() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findByEmail("user@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("user@example.com", result.get().getEmail());
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(user, new User());
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void updateUser_Success() {
        // Arrange
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updateDetails = new User();
        updateDetails.setName("Updated Name");
        updateDetails.setAddress("456 New St");

        // Act
        User result = userService.updateUser("user-id", updateDetails);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser("not-found", new User());
        });
    }

    @Test
    void deleteUser_Success() {
        // Act
        userService.deleteUser("user-id");

        // Assert
        verify(userRepository).deleteById("user-id");
    }
}
