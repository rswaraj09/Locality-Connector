package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/business-data")
@RequiredArgsConstructor
public class BusinessDataController {
    
    private final BusinessRepository businessRepository;
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllBusinesses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "businessName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Business> businesses = businessRepository.findAll(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("businesses", businesses.getContent());
            response.put("currentPage", businesses.getNumber());
            response.put("totalPages", businesses.getTotalPages());
            response.put("totalElements", businesses.getTotalElements());
            response.put("size", businesses.getSize());
            response.put("first", businesses.isFirst());
            response.put("last", businesses.isLast());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch businesses: " + e.getMessage()));
        }
    }
    
    @GetMapping("/by-state")
    public ResponseEntity<?> getBusinessesByState(@RequestParam String state) {
        try {
            // This is a simplified approach - in a real app you'd have a state field
            List<Business> businesses = businessRepository.findAll();
            
            // Filter by state based on coordinates (approximate)
            List<Business> stateBusinesses = businesses.stream()
                .filter(business -> isInState(business, state))
                .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("state", state);
            response.put("count", stateBusinesses.size());
            response.put("businesses", stateBusinesses);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch businesses by state: " + e.getMessage()));
        }
    }
    
    @GetMapping("/by-category")
    public ResponseEntity<?> getBusinessesByCategory(@RequestParam String category) {
        try {
            List<Business> businesses = businessRepository.findByCategory(category);
            
            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("count", businesses.size());
            response.put("businesses", businesses);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch businesses by category: " + e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchBusinesses(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<Business> allBusinesses = businessRepository.findAll();
            
            // Simple search by business name, address, or category
            List<Business> filteredBusinesses = allBusinesses.stream()
                .filter(business -> 
                    business.getBusinessName().toLowerCase().contains(query.toLowerCase()) ||
                    business.getAddress().toLowerCase().contains(query.toLowerCase()) ||
                    business.getCategory().toLowerCase().contains(query.toLowerCase())
                )
                .toList();
            
            // Simple pagination
            int start = page * size;
            int end = Math.min(start + size, filteredBusinesses.size());
            List<Business> pageResults = filteredBusinesses.subList(start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("businesses", pageResults);
            response.put("currentPage", page);
            response.put("totalPages", (int) Math.ceil((double) filteredBusinesses.size() / size));
            response.put("totalElements", filteredBusinesses.size());
            response.put("size", size);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<?> getBusinessStats() {
        try {
            List<Business> allBusinesses = businessRepository.findAll();
            
            Map<String, Long> categoryStats = allBusinesses.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Business::getCategory, 
                    java.util.stream.Collectors.counting()
                ));
            
            Map<String, Long> stateStats = allBusinesses.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    this::getStateFromCoordinates,
                    java.util.stream.Collectors.counting()
                ));
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBusinesses", allBusinesses.size());
            stats.put("categoryDistribution", categoryStats);
            stats.put("stateDistribution", stateStats);
            stats.put("businessesWithCoordinates", allBusinesses.stream()
                .filter(b -> b.getLatitude() != null && b.getLongitude() != null)
                .count());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Stats calculation failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/export")
    public ResponseEntity<?> exportAllBusinesses() {
        try {
            List<Business> businesses = businessRepository.findAll();
            return ResponseEntity.ok(businesses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Export failed: " + e.getMessage()));
        }
    }
    
    private boolean isInState(Business business, String state) {
        if (business.getLatitude() == null || business.getLongitude() == null) {
            return false;
        }
        
        double lat = business.getLatitude();
        double lng = business.getLongitude();
        
        // Approximate state boundaries (simplified)
        switch (state.toLowerCase()) {
            case "maharashtra":
                return lat >= 15.0 && lat <= 22.0 && lng >= 72.0 && lng <= 80.0;
            case "karnataka":
                return lat >= 11.0 && lat <= 18.0 && lng >= 74.0 && lng <= 78.0;
            case "tamil nadu":
                return lat >= 8.0 && lat <= 13.0 && lng >= 76.0 && lng <= 80.0;
            case "gujarat":
                return lat >= 20.0 && lat <= 24.0 && lng >= 68.0 && lng <= 74.0;
            case "rajasthan":
                return lat >= 23.0 && lat <= 30.0 && lng >= 69.0 && lng <= 78.0;
            case "uttar pradesh":
                return lat >= 24.0 && lat <= 31.0 && lng >= 77.0 && lng <= 84.0;
            case "west bengal":
                return lat >= 21.0 && lat <= 27.0 && lng >= 85.0 && lng <= 89.0;
            case "kerala":
                return lat >= 8.0 && lat <= 13.0 && lng >= 74.0 && lng <= 77.0;
            case "telangana":
                return lat >= 15.0 && lat <= 19.0 && lng >= 77.0 && lng <= 81.0;
            case "delhi ncr":
                return lat >= 28.0 && lat <= 29.0 && lng >= 76.0 && lng <= 78.0;
            default:
                return false;
        }
    }
    
    private String getStateFromCoordinates(Business business) {
        if (business.getLatitude() == null || business.getLongitude() == null) {
            return "Unknown";
        }
        
        double lat = business.getLatitude();
        double lng = business.getLongitude();
        
        if (lat >= 15.0 && lat <= 22.0 && lng >= 72.0 && lng <= 80.0) return "Maharashtra";
        if (lat >= 11.0 && lat <= 18.0 && lng >= 74.0 && lng <= 78.0) return "Karnataka";
        if (lat >= 8.0 && lat <= 13.0 && lng >= 76.0 && lng <= 80.0) return "Tamil Nadu";
        if (lat >= 20.0 && lat <= 24.0 && lng >= 68.0 && lng <= 74.0) return "Gujarat";
        if (lat >= 23.0 && lat <= 30.0 && lng >= 69.0 && lng <= 78.0) return "Rajasthan";
        if (lat >= 24.0 && lat <= 31.0 && lng >= 77.0 && lng <= 84.0) return "Uttar Pradesh";
        if (lat >= 21.0 && lat <= 27.0 && lng >= 85.0 && lng <= 89.0) return "West Bengal";
        if (lat >= 8.0 && lat <= 13.0 && lng >= 74.0 && lng <= 77.0) return "Kerala";
        if (lat >= 15.0 && lat <= 19.0 && lng >= 77.0 && lng <= 81.0) return "Telangana";
        if (lat >= 28.0 && lat <= 29.0 && lng >= 76.0 && lng <= 78.0) return "Delhi NCR";
        
        return "Other";
    }
}



