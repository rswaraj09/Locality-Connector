package com.example.localityconnector.controller;

import com.example.localityconnector.service.OrderService;
import com.example.localityconnector.service.FeedbackService;
import com.example.localityconnector.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/business/dashboard")
@RequiredArgsConstructor
public class BusinessDashboardController {
    
    private final OrderService orderService;
    private final FeedbackService feedbackService;
    private final UserService userService;
    
    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(HttpSession session) {
        try {
            String businessId = (String) session.getAttribute("loggedInBusinessId");
            if (businessId == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Business not logged in"));
            }
            return ResponseEntity.ok(orderService.getOrdersByBusinessId(businessId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/orders/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status, HttpSession session) {
        try {
            String businessId = (String) session.getAttribute("loggedInBusinessId");
            if (businessId == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Business not logged in"));
            }
            return ResponseEntity.ok(orderService.getOrdersByBusinessIdAndStatus(businessId, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/feedback")
    public ResponseEntity<?> getFeedback(HttpSession session) {
        try {
            String businessId = (String) session.getAttribute("loggedInBusinessId");
            if (businessId == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Business not logged in"));
            }
            return ResponseEntity.ok(feedbackService.getFeedbackByBusinessId(businessId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/customers")
    public ResponseEntity<?> getCustomers(HttpSession session) {
        try {
            String businessId = (String) session.getAttribute("loggedInBusinessId");
            if (businessId == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Business not logged in"));
            }
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String orderId, @RequestParam String status) {
        try {
            return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}

