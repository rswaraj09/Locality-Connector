package com.example.localityconnector.service;

import com.example.localityconnector.model.Business;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoordinateAccuracyService {
    
    public Map<String, Object> validateBusinessCoordinates(Business business) {
        Map<String, Object> result = new HashMap<>();
        List<String> issues = new ArrayList<>();
        boolean isValid = true;
        
        // Check if coordinates exist
        if (business.getLatitude() == null || business.getLongitude() == null) {
            issues.add("Missing coordinates");
            isValid = false;
            result.put("valid", false);
            result.put("issues", issues);
            return result;
        }
        
        double lat = business.getLatitude();
        double lng = business.getLongitude();
        
        // Check India bounds
        if (lat < 6.0 || lat > 37.0) {
            issues.add("Latitude outside India bounds (6.0 to 37.0)");
            isValid = false;
        }
        
        if (lng < 68.0 || lng > 97.0) {
            issues.add("Longitude outside India bounds (68.0 to 97.0)");
            isValid = false;
        }
        
        // Check precision
        String latStr = String.valueOf(lat);
        String lngStr = String.valueOf(lng);
        
        int latPrecision = latStr.contains(".") ? latStr.split("\\.")[1].length() : 0;
        int lngPrecision = lngStr.contains(".") ? lngStr.split("\\.")[1].length() : 0;
        
        if (latPrecision < 4) {
            issues.add("Low latitude precision: " + latPrecision + " decimal places (minimum 4 recommended)");
        }
        
        if (lngPrecision < 4) {
            issues.add("Low longitude precision: " + lngPrecision + " decimal places (minimum 4 recommended)");
        }
        
        // Check for reasonable accuracy (within ~11 meters for 4 decimal places)
        if (latPrecision >= 4 && lngPrecision >= 4) {
            result.put("accuracy", "High (within ~11 meters)");
        } else if (latPrecision >= 3 && lngPrecision >= 3) {
            result.put("accuracy", "Medium (within ~111 meters)");
        } else {
            result.put("accuracy", "Low (within ~1.1 km)");
        }
        
        result.put("valid", isValid);
        result.put("issues", issues);
        result.put("latitude", lat);
        result.put("longitude", lng);
        result.put("latitude_precision", latPrecision);
        result.put("longitude_precision", lngPrecision);
        result.put("business_name", business.getBusinessName());
        result.put("business_id", business.getId());
        result.put("category", business.getCategory());
        result.put("address", business.getAddress());
        
        return result;
    }
    
    public Map<String, Object> validateAllBusinesses(List<Business> businesses) {
        Map<String, Object> summary = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        
        int totalBusinesses = businesses.size();
        int validBusinesses = 0;
        int highAccuracyBusinesses = 0;
        int mediumAccuracyBusinesses = 0;
        int lowAccuracyBusinesses = 0;
        
        for (Business business : businesses) {
            Map<String, Object> validation = validateBusinessCoordinates(business);
            results.add(validation);
            
            if ((Boolean) validation.get("valid")) {
                validBusinesses++;
                String accuracy = (String) validation.get("accuracy");
                if (accuracy.contains("High")) {
                    highAccuracyBusinesses++;
                } else if (accuracy.contains("Medium")) {
                    mediumAccuracyBusinesses++;
                } else {
                    lowAccuracyBusinesses++;
                }
            }
        }
        
        summary.put("total_businesses", totalBusinesses);
        summary.put("valid_businesses", validBusinesses);
        summary.put("invalid_businesses", totalBusinesses - validBusinesses);
        summary.put("high_accuracy", highAccuracyBusinesses);
        summary.put("medium_accuracy", mediumAccuracyBusinesses);
        summary.put("low_accuracy", lowAccuracyBusinesses);
        summary.put("validation_rate", (double) validBusinesses / totalBusinesses * 100);
        summary.put("high_accuracy_rate", (double) highAccuracyBusinesses / totalBusinesses * 100);
        summary.put("detailed_results", results);
        
        return summary;
    }
    
    public double calculateDistanceAccuracy(double lat1, double lng1, double lat2, double lng2) {
        // Haversine formula for distance calculation
        final int R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c * 1000; // Return distance in meters
    }
}
