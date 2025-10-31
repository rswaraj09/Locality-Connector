package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {
    
    private final BusinessRepository businessRepository;
    
    @GetMapping("/raw-business")
    public ResponseEntity<?> getRawBusiness() {
        try {
            List<Business> businesses = businessRepository.findAll();
            if (businesses.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "No businesses found"));
            }
            
            Business business = businesses.get(0);
            
            // Create a manual map to see exactly what's being serialized
            Map<String, Object> debugInfo = Map.of(
                "id", business.getId() != null ? business.getId() : "NULL",
                "businessName", business.getBusinessName() != null ? business.getBusinessName() : "NULL",
                "ownerName", business.getOwnerName() != null ? business.getOwnerName() : "NULL",
                "email", business.getEmail() != null ? business.getEmail() : "NULL",
                "address", business.getAddress() != null ? business.getAddress() : "NULL",
                "category", business.getCategory() != null ? business.getCategory() : "NULL",
                "latitude", business.getLatitude() != null ? business.getLatitude() : "NULL",
                "longitude", business.getLongitude() != null ? business.getLongitude() : "NULL",
                "phoneNumber", business.getPhoneNumber() != null ? business.getPhoneNumber() : "NULL",
                "isActive", business.isActive()
            );
            
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/business-count")
    public ResponseEntity<?> getBusinessCount() {
        try {
            long count = businessRepository.count();
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}



