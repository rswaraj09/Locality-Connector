package com.example.localityconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * In-app notification for users and business owners.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
@CompoundIndex(name = "recipient_read_idx", def = "{'recipientId': 1, 'read': 1, 'createdAt': -1}")
public class Notification {
    @Id
    private String id;

    @Indexed
    private String recipientId;      // userId or businessId
    private String recipientType;    // "USER" or "BUSINESS"
    private String title;
    private String message;
    private String type;             // FEEDBACK_RECEIVED, BUSINESS_VERIFIED, BUSINESS_SUSPENDED, REVIEW_FLAGGED, etc.
    private String referenceId;      // optional link to the related entity
    private boolean read = false;
    private Date createdAt;

    public void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }
}
