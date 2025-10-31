package com.example.localityconnector.controller;

import com.example.localityconnector.service.DataSeedingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class DataSeedingController {
    
    private final DataSeedingService dataSeedingService;
    
    @PostMapping("/seed-data")
    public ResponseEntity<?> seedData() {
        try {
            dataSeedingService.seedBusinessData();
            return ResponseEntity.ok(Map.of("message", "Data seeding completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Data seeding failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/seed-city")
    public ResponseEntity<?> seedSpecificCity(
            @RequestParam String cityName,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        try {
            dataSeedingService.seedSpecificCity(cityName, latitude, longitude);
            return ResponseEntity.ok(Map.of("message", "City data seeding completed for " + cityName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "City data seeding failed: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/clear-data")
    public ResponseEntity<?> clearData() {
        try {
            dataSeedingService.clearAllData();
            return ResponseEntity.ok(Map.of("message", "All data cleared successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Data clearing failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/clear-and-reseed")
    public ResponseEntity<?> clearAndReseedData() {
        try {
            dataSeedingService.clearAndReseedData();
            return ResponseEntity.ok(Map.of("message", "Data cleared and re-seeded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Clear and re-seed failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/remove-duplicates")
    public ResponseEntity<?> removeDuplicates() {
        try {
            dataSeedingService.removeDuplicates();
            return ResponseEntity.ok(Map.of("message", "Duplicate businesses removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Duplicate removal failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/seed-budget")
    public ResponseEntity<?> seedBudgetData() {
        try {
            dataSeedingService.seedBusinessDataBudget();
            return ResponseEntity.ok(Map.of("message", "Budget-friendly data seeding completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Budget seeding failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/seed-full")
    public ResponseEntity<?> seedFullData() {
        try {
            dataSeedingService.seedBusinessDataFull();
            return ResponseEntity.ok(Map.of("message", "Full data seeding completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Full seeding failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        try {
            long businessCount = dataSeedingService.getBusinessCount();
            return ResponseEntity.ok(Map.of(
                "businessCount", businessCount,
                "status", businessCount > 0 ? "Data available" : "No data"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status check failed: " + e.getMessage()));
        }
    }
}


