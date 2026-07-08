package com.example.localityconnector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Business {

    private String id;

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    private String businessName;

    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 50, message = "Owner name must be between 2 and 50 characters")
    private String ownerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    // WRITE_ONLY: the password hash must never be serialized into API responses
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Business address is required")
    private String address;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Business category is required")
    private String category; // e.g., "food", "pharmacy", "clothing", "stationary", "hospital"

    private String description;

    private String businessLicense;

    // Optional geolocation fields
    private Double latitude;

    private Double longitude;

    // Geohash (precision 6 = ~1.2km cell) used for Firestore proximity prefix queries.
    // Computed from latitude/longitude whenever coordinates are saved.
    private String geohash;

    // Native Firestore timestamps (serialized as Timestamp, not String).
    private Date createdAt;

    private Date updatedAt;

    private boolean isActive = true;

    private boolean isVerified = false;

    private boolean emailVerified = false;

    // Denormalized rating counters — updated atomically when feedback is submitted/deleted.
    private long ratingSum = 0;
    private int ratingCount = 0;
    private double averageRating = 0.0;

    // Analytics counters
    private long viewsCount = 0;
    private long clicksCount = 0;
    private long favoritesCount = 0;

    // Business logo URL (Firebase Storage)
    private String logoUrl;

    // Storefront image URL
    private String storefrontUrl;

    // Multiple business photos URLs
    private List<String> photoUrls = new ArrayList<>();

    private Map<String, Boolean> notificationPreferences = new HashMap<>();

    // Pre-save method to set timestamps
    public void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        updatedAt = new Date();
    }
}
