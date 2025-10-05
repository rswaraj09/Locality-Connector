package com.example.localityconnector.controller;

import com.example.localityconnector.dto.LocationBasedBusinessRequest;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.ItemService;
import com.example.localityconnector.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/dashboard")
@RequiredArgsConstructor
public class UserDashboardController {
    
    private final BusinessService businessService;
    private final ItemService itemService;
    private final OrderService orderService;
    
    @GetMapping("/businesses")
    public ResponseEntity<?> getBusinesses() {
        try {
            return ResponseEntity.ok(businessService.getAllBusinesses());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/businesses/category/{category}")
    public ResponseEntity<?> getBusinessesByCategory(@PathVariable String category) {
        try {
            return ResponseEntity.ok(businessService.getBusinessesByCategory(category));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/items/business/{businessId}")
    public ResponseEntity<?> getItemsByBusiness(@PathVariable String businessId) {
        try {
            return ResponseEntity.ok(itemService.getItemsByBusinessId(businessId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/orders/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/businesses/nearby")
    public ResponseEntity<?> getNearbyBusinesses(@RequestBody LocationBasedBusinessRequest request) {
        try {
            if (request.getLatitude() == null || request.getLongitude() == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Latitude and longitude are required"));
            }
            
            return ResponseEntity.ok(businessService.getBusinessesWithinRadius(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/businesses/nearby")
    public ResponseEntity<?> getNearbyBusinesses(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm,
            @RequestParam(required = false) String category) {
        try {
            LocationBasedBusinessRequest request = new LocationBasedBusinessRequest(latitude, longitude, radiusKm);
            if (category != null && !category.isEmpty()) {
                request.setCategory(category);
            }
            
            return ResponseEntity.ok(businessService.getBusinessesWithinRadius(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}

