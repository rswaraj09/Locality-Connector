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
 * A user's bookmarked/favourited business.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "favorites")
@CompoundIndex(name = "user_business_idx", def = "{'userId': 1, 'businessId': 1}", unique = true)
public class Favorite {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
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
