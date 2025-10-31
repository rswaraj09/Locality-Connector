package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.repository.BusinessRepository;
import com.example.localityconnector.service.CoordinateAccuracyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accuracy")
@RequiredArgsConstructor
public class CoordinateAccuracyController {
    
    private final BusinessRepository businessRepository;
    private final CoordinateAccuracyService coordinateAccuracyService;
    
    @GetMapping("/validate-all")
    public ResponseEntity<?> validateAllBusinesses() {
        try {
            List<Business> businesses = businessRepository.findAll();
            Map<String, Object> results = coordinateAccuracyService.validateAllBusinesses(businesses);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Validation failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/validate/{businessId}")
    public ResponseEntity<?> validateBusiness(@PathVariable String businessId) {
        try {
            Business business = businessRepository.findById(businessId).orElse(null);
            if (business == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> result = coordinateAccuracyService.validateBusinessCoordinates(business);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Validation failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/sample-validation")
    public ResponseEntity<?> sampleValidation(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Business> businesses = businessRepository.findAll().stream()
                    .limit(limit)
                    .toList();
            
            Map<String, Object> results = coordinateAccuracyService.validateAllBusinesses(businesses);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sample validation failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/accuracy-stats")
    public ResponseEntity<?> getAccuracyStats() {
        try {
            List<Business> businesses = businessRepository.findAll();
            Map<String, Object> results = coordinateAccuracyService.validateAllBusinesses(businesses);
            
            // Extract key statistics
            Map<String, Object> stats = Map.of(
                "total_businesses", results.get("total_businesses"),
                "valid_businesses", results.get("valid_businesses"),
                "validation_rate", results.get("validation_rate"),
                "high_accuracy_rate", results.get("high_accuracy_rate"),
                "high_accuracy_count", results.get("high_accuracy"),
                "medium_accuracy_count", results.get("medium_accuracy"),
                "low_accuracy_count", results.get("low_accuracy")
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Stats calculation failed: " + e.getMessage()));
        }
    }
}



