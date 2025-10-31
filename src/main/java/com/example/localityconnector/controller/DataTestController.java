package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class DataTestController {
    
    private final BusinessRepository businessRepository;
    
    @GetMapping("/businesses-sample")
    public ResponseEntity<?> getSampleBusinesses(@RequestParam(defaultValue = "5") int limit) {
        try {
            List<Business> businesses = businessRepository.findAll().stream()
                    .limit(limit)
                    .toList();
            
            return ResponseEntity.ok(Map.of(
                "count", businesses.size(),
                "businesses", businesses
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/business-fields")
    public ResponseEntity<?> getBusinessFields(@RequestParam(defaultValue = "1") int limit) {
        try {
            List<Business> businesses = businessRepository.findAll().stream()
                    .limit(limit)
                    .toList();
            
            if (businesses.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "No businesses found"));
            }
            
            Business business = businesses.get(0);
            return ResponseEntity.ok(Map.of(
                "id", business.getId(),
                "businessName", business.getBusinessName(),
                "ownerName", business.getOwnerName(),
                "email", business.getEmail(),
                "address", business.getAddress(),
                "category", business.getCategory(),
                "latitude", business.getLatitude(),
                "longitude", business.getLongitude(),
                "phoneNumber", business.getPhoneNumber(),
                "isActive", business.isActive()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}



