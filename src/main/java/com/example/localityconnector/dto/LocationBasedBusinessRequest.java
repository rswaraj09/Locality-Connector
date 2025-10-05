package com.example.localityconnector.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationBasedBusinessRequest {
    
    private Double latitude;
    private Double longitude;
    private Double radiusKm = 5.0; // Default 5km radius
    private String category; // Optional category filter
    
    public LocationBasedBusinessRequest(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusKm = 5.0;
    }
    
    public LocationBasedBusinessRequest(Double latitude, Double longitude, Double radiusKm) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusKm = radiusKm;
    }
}


