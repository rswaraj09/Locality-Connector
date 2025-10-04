package com.example.localityconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "feedback")
public class Feedback {
    
    @Id
    private String id;
    
    private String userId;
    private String userName;
    private String userEmail;
    
    private String businessId;
    private String businessName;
    
    private String orderId;
    
    private Integer rating; // 1-5 stars
    private String comment;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
}

