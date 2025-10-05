package com.example.localityconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts")
public class Cart {
    
    @Id
    private String id;
    
    private String userId;
    private String userName;
    private String userEmail;
    
    private List<CartItem> items;
    
    private Double totalAmount;
    private Integer totalItems;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    public void calculateTotals() {
        if (items != null) {
            totalItems = items.stream().mapToInt(CartItem::getQuantity).sum();
            totalAmount = items.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        } else {
            totalItems = 0;
            totalAmount = 0.0;
        }
    }
}



