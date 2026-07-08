package com.example.localityconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * Token used for password reset. Stored in the {@code password_reset_tokens}
 * Firestore collection. Expires after 1 hour.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    private String id;
    private String email;
    private String entityType;     // "USER" or "BUSINESS"
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
