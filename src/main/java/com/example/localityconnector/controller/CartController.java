package com.example.localityconnector.controller;

import com.example.localityconnector.model.Cart;
import com.example.localityconnector.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<?> getCart(@PathVariable String userId) {
        try {
            Cart cart = cartService.getCart(userId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{userId}/add")
    public ResponseEntity<?> addItemToCart(
            @PathVariable String userId,
            @RequestParam String itemId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            Cart cart = cartService.addItemToCart(userId, itemId, quantity);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{userId}/remove/{itemId}")
    public ResponseEntity<?> removeItemFromCart(
            @PathVariable String userId,
            @PathVariable String itemId) {
        try {
            Cart cart = cartService.removeItemFromCart(userId, itemId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{userId}/update/{itemId}")
    public ResponseEntity<?> updateItemQuantity(
            @PathVariable String userId,
            @PathVariable String itemId,
            @RequestParam Integer quantity) {
        try {
            Cart cart = cartService.updateItemQuantity(userId, itemId, quantity);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable String userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}



