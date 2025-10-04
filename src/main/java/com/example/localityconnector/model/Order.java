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
@Document(collection = "orders")
public class Order {
    
    @Id
    private String id;
    
    private String userId;
    private String userName;
    private String userEmail;
    private String userAddress;
    private String userPhone;
    
    private String businessId;
    private String businessName;
    
    private List<OrderItem> items;
    
    private Double totalAmount;
    private String status; // PENDING, CONFIRMED, DELIVERED, CANCELLED
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
}

