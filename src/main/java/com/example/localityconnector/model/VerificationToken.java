package com.example.localityconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Token used for email verification on signup. Expires after 24 hours.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "verification_tokens")
public class VerificationToken {
    @Id
    private String id;

    @Indexed
    private String entityId;       // userId or businessId

    private String entityType;     // "USER" or "BUSINESS"

    @Indexed
    private String email;

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
