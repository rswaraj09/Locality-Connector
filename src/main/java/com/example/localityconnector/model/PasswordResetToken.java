package com.example.localityconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Token used for password reset. Expires after 1 hour.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    private String id;

    @Indexed
    private String email;

    private String entityType;     // "USER" or "BUSINESS"

    @Indexed(unique = true)
    private String token;          // UUID

    @Indexed
    private Date expiresAt;

    private boolean used = false;
    private Date createdAt;

    public void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }
}
