package com.example.localityconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * In-app notification for users and business owners. Stored in the
 * {@code notifications} Firestore collection.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private String id;
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
