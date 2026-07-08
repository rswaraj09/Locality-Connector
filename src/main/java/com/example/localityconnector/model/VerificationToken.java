package com.example.localityconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * Token used for email verification on signup. Stored in the
 * {@code verification_tokens} Firestore collection. Expires after 24 hours.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationToken {
    private String id;
    private String entityId;       // userId or businessId
    private String entityType;     // "USER" or "BUSINESS"
    private String email;
    private String token;          // UUID
    private Date expiresAt;
    private boolean used = false;
    private Date createdAt;

    public void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }
}
