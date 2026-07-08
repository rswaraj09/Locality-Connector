package com.example.localityconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * A user's bookmarked/favourited business. Stored in the {@code favorites}
 * Firestore collection.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {
    private String id;
    private String userId;
    private String businessId;
    private String businessName;
    private String businessCategory;
    private Date createdAt;

    public void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }
}
