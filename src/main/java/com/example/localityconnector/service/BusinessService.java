package com.example.localityconnector.service;

import com.example.localityconnector.dto.BusinessSignupRequest;
import com.example.localityconnector.dto.LocationBasedBusinessRequest;
import com.example.localityconnector.model.Business;
import com.example.localityconnector.repository.BusinessRepository;
import com.example.localityconnector.util.GeolocationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessService {
    
    private final BusinessRepository businessRepository;
    
    public Business signup(BusinessSignupRequest request) {
        // Check if business already exists
        if (businessRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Business with this email already exists");
        }
        
        if (businessRepository.existsByBusinessName(request.getBusinessName())) {
            throw new RuntimeException("Business with this name already exists");
        }
        
        // Create new business
        Business business = new Business();
        business.setBusinessName(request.getBusinessName());
        business.setOwnerName(request.getOwnerName());
        business.setEmail(request.getEmail());
        business.setPassword(request.getPassword()); // In production, this should be encrypted
        business.setAddress(request.getAddress());
        business.setPhoneNumber(request.getPhoneNumber());
        business.setCategory(request.getCategory());
        business.setDescription(request.getDescription());
        business.setBusinessLicense(request.getBusinessLicense());
        
        // Coordinates can be set later; geocoding removed
        
        // Set timestamps
        business.prePersist();
        
        try {
            return businessRepository.save(business);
        } catch (DuplicateKeyException ex) {
            // Handle race conditions against unique indexes (email, businessName)
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("email")) {
                throw new RuntimeException("Email already exists");
            }
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("businessname")) {
                throw new RuntimeException("Business name already exists");
            }
            throw new RuntimeException("Duplicate key error");
        }
    }
    
    public Optional<Business> login(String email, String password) {
        return businessRepository.findByEmailAndPassword(email, password);
    }
    
    public Optional<Business> findByEmail(String email) {
        return businessRepository.findByEmail(email);
    }
    
    public Optional<Business> findByBusinessName(String businessName) {
        return businessRepository.findByBusinessName(businessName);
    }
    
    public List<Business> getAllBusinesses() {
        return businessRepository.findAll();
    }
    
    public List<Business> getBusinessesByCategory(String category) {
        return businessRepository.findByCategoryAndIsActiveTrue(category);
    }
    
    public Optional<Business> findById(String id) {
        return businessRepository.findById(id);
    }
    
    public Business updateBusiness(String id, Business businessDetails) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        
        business.setBusinessName(businessDetails.getBusinessName());
        business.setOwnerName(businessDetails.getOwnerName());
        business.setAddress(businessDetails.getAddress());
        business.setPhoneNumber(businessDetails.getPhoneNumber());
        business.setCategory(businessDetails.getCategory());
        business.setDescription(businessDetails.getDescription());
        business.setBusinessLicense(businessDetails.getBusinessLicense());
        
        // Geocoding removed; keep existing coordinates if present
        
        business.setUpdatedAt(java.time.LocalDateTime.now());
        
        return businessRepository.save(business);
    }
    
    public Business verifyBusiness(String id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        
        business.setVerified(true);
        business.setUpdatedAt(java.time.LocalDateTime.now());
        
        return businessRepository.save(business);
    }
    
    public void deleteBusiness(String id) {
        businessRepository.deleteById(id);
    }
    
    public List<Business> getBusinessesWithinRadius(LocationBasedBusinessRequest request) {
        List<Business> allBusinesses = businessRepository.findAll();
        
        // Check if any businesses have coordinates
        boolean hasBusinessesWithCoords = allBusinesses.stream()
                .anyMatch(business -> business.getLatitude() != null && business.getLongitude() != null);
        
        // If no businesses have coordinates, return all businesses
        if (!hasBusinessesWithCoords) {
            return allBusinesses.stream()
                    .filter(business -> {
                        // Apply category filter if specified
                        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
                            return business.getCategory().equalsIgnoreCase(request.getCategory());
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }
        
        // Filter businesses with coordinates within radius
        return allBusinesses.stream()
                .filter(business -> {
                    if (business.getLatitude() == null || business.getLongitude() == null) {
                        return false; // Skip businesses without coordinates
                    }
                    
                    // Check if within radius
                    boolean withinRadius = GeolocationUtils.isWithinRadius(
                            request.getLatitude(), 
                            request.getLongitude(),
                            business.getLatitude(), 
                            business.getLongitude(), 
                            request.getRadiusKm()
                    );
                    
                    // Apply category filter if specified
                    if (withinRadius && request.getCategory() != null && !request.getCategory().isEmpty()) {
                        return business.getCategory().equalsIgnoreCase(request.getCategory());
                    }
                    
                    return withinRadius;
                })
                .collect(Collectors.toList());
    }
    
    public List<Business> getBusinessesWithinRadius(double latitude, double longitude, double radiusKm) {
        LocationBasedBusinessRequest request = new LocationBasedBusinessRequest(latitude, longitude, radiusKm);
        return getBusinessesWithinRadius(request);
    }
    
    public List<Business> getBusinessesWithinRadius(double latitude, double longitude) {
        return getBusinessesWithinRadius(latitude, longitude, 5.0);
    }
}
























