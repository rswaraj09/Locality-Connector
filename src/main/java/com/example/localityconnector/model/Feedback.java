package com.example.localityconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    private String id;

    private String userId;
    private String userName;
    private String userEmail;

    private String businessId;
    private String businessName;

    private String orderId;

    // Star rating, 1-5.
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String comment;

    private Date createdAt;
    private Date updatedAt;

    // Admin moderation fields
    private boolean flagged = false;
    private String moderationNote;

    public void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        updatedAt = new Date();
    }
}
