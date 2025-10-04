package com.example.localityconnector.controller;

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
}

