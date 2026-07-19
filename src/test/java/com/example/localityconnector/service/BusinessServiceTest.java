package com.example.localityconnector.service;

import com.example.localityconnector.dto.BusinessSignupRequest;
import com.example.localityconnector.exception.DuplicateResourceException;
import com.example.localityconnector.exception.ResourceNotFoundException;
import com.example.localityconnector.model.Business;
import com.example.localityconnector.repository.BusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private GooglePlacesService googlePlacesService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ItemService itemService;

    @Mock
    private FeedbackService feedbackService;

    @InjectMocks
    private BusinessService businessService;

    private BusinessSignupRequest signupRequest;
    private Business business;

    @BeforeEach
    void setUp() {
        signupRequest = new BusinessSignupRequest();
        signupRequest.setBusinessName("Test Business");
        signupRequest.setOwnerName("John Doe");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setAddress("123 Main St");
        signupRequest.setPhoneNumber("1234567890");
        signupRequest.setCategory("food");

        business = new Business();
        business.setId("test-id");
        business.setBusinessName("Test Business");
        business.setEmail("test@example.com");
        business.setPassword("hashedPassword");
    }

    @Test
    void signup_Success() {
        // Arrange
        when(businessRepository.existsByEmail(anyString())).thenReturn(false);
        when(businessRepository.existsByBusinessName(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(businessRepository.save(any(Business.class))).thenReturn(business);

        // Act
        Business result = businessService.signup(signupRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Test Business", result.getBusinessName());
        verify(passwordEncoder).encode("password123");
        verify(businessRepository).save(any(Business.class));
    }

    @Test
    void signup_DuplicateEmail_ThrowsException() {
        // Arrange
        when(businessRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            businessService.signup(signupRequest);
        });
        verify(businessRepository, never()).save(any(Business.class));
    }

    @Test
    void signup_DuplicateBusinessName_ThrowsException() {
        // Arrange
        when(businessRepository.existsByEmail(anyString())).thenReturn(false);
        when(businessRepository.existsByBusinessName(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            businessService.signup(signupRequest);
        });
        verify(businessRepository, never()).save(any(Business.class));
    }

    @Test
    void login_Success() {
        // Arrange
        when(businessRepository.findByEmail(anyString())).thenReturn(Optional.of(business));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        Optional<Business> result = businessService.login("test@example.com", "password123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(passwordEncoder).matches("password123", "hashedPassword");
    }

    @Test
    void login_InvalidPassword_ReturnsEmpty() {
        // Arrange
        when(businessRepository.findByEmail(anyString())).thenReturn(Optional.of(business));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act
        Optional<Business> result = businessService.login("test@example.com", "wrongpassword");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void login_UserNotFound_ReturnsEmpty() {
        // Arrange
        when(businessRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<Business> result = businessService.login("notfound@example.com", "password123");

        // Assert
        assertFalse(result.isPresent());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void findById_Success() {
        // Arrange
        when(businessRepository.findById(anyString())).thenReturn(Optional.of(business));

        // Act
        Optional<Business> result = businessService.findById("test-id");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test-id", result.get().getId());
    }

    @Test
    void updateBusiness_Success() {
        // Arrange
        when(businessRepository.findById(anyString())).thenReturn(Optional.of(business));
        when(businessRepository.save(any(Business.class))).thenReturn(business);

        Business updateDetails = new Business();
        updateDetails.setBusinessName("Updated Business");
        updateDetails.setAddress("456 New St");

        // Act
        Business result = businessService.updateBusiness("test-id", updateDetails);

        // Assert
        assertNotNull(result);
        verify(businessRepository).save(any(Business.class));
    }

    @Test
    void updateBusiness_NotFound_ThrowsException() {
        // Arrange
        when(businessRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            businessService.updateBusiness("not-found", new Business());
        });
    }

    @Test
    void deleteBusiness_Success() {
        // Act
        businessService.deleteBusiness("test-id");

        // Assert
        verify(businessRepository).deleteById("test-id");
    }

    @Test
    void verifyBusiness_Success() {
        // Arrange
        when(businessRepository.findById(anyString())).thenReturn(Optional.of(business));
        when(businessRepository.save(any(Business.class))).thenReturn(business);

        // Act
        Business result = businessService.verifyBusiness("test-id");

        // Assert
        assertNotNull(result);
        verify(businessRepository).save(any(Business.class));
    }
}
