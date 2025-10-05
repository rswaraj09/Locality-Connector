package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/business/location")
@RequiredArgsConstructor
public class BusinessLocationController {
    
    private final BusinessService businessService;
    private final GeocodingService geocodingService = new GeocodingService();
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllBusinesses() {
        try {
            List<Business> businesses = businessService.getAllBusinesses();
            return ResponseEntity.ok(businesses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/without-coordinates")
    public ResponseEntity<?> getBusinessesWithoutCoordinates() {
        try {
            List<Business> allBusinesses = businessService.getAllBusinesses();
            List<Business> businessesWithoutCoords = allBusinesses.stream()
                    .filter(business -> business.getLatitude() == null || business.getLongitude() == null)
                    .toList();
            return ResponseEntity.ok(businessesWithoutCoords);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/set-coordinates/{businessId}")
    public ResponseEntity<?> setBusinessCoordinates(
            @PathVariable String businessId,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        try {
            var businessOpt = businessService.findById(businessId);
            if (businessOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Business not found"));
            }
            
            Business business = businessOpt.get();
            business.setLatitude(latitude);
            business.setLongitude(longitude);
            business.setUpdatedAt(java.time.LocalDateTime.now());
            
            Business updatedBusiness = businessService.updateBusiness(businessId, business);
            return ResponseEntity.ok(Map.of(
                "message", "Coordinates updated successfully",
                "business", updatedBusiness
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/geocode/{businessId}")
    public ResponseEntity<?> geocodeBusinessByAddress(@PathVariable String businessId) {
        try {
            var businessOpt = businessService.findById(businessId);
            if (businessOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Business not found"));
            }
            Business business = businessOpt.get();
            var geo = geocodingService.geocodeAddress(business.getAddress()).block();
            if (geo == null || !geo.ok()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to geocode business address"));
            }
            business.setLatitude(geo.lat());
            business.setLongitude(geo.lon());
            business.setUpdatedAt(java.time.LocalDateTime.now());
            Business updatedBusiness = businessService.updateBusiness(businessId, business);
            return ResponseEntity.ok(Map.of("message", "Coordinates updated via geocoding", "business", updatedBusiness));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/test-radius")
    public ResponseEntity<?> testRadiusFiltering(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {
        try {
            List<Business> nearbyBusinesses = businessService.getBusinessesWithinRadius(latitude, longitude, radiusKm);
            List<Business> allBusinesses = businessService.getAllBusinesses();
            
            return ResponseEntity.ok(Map.of(
                "userLocation", Map.of("latitude", latitude, "longitude", longitude),
                "radiusKm", radiusKm,
                "nearbyBusinesses", nearbyBusinesses,
                "totalBusinesses", allBusinesses.size(),
                "businessesWithCoords", allBusinesses.stream()
                    .filter(b -> b.getLatitude() != null && b.getLongitude() != null)
                    .count(),
                "businessesWithoutCoords", allBusinesses.stream()
                    .filter(b -> b.getLatitude() == null || b.getLongitude() == null)
                    .count()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
