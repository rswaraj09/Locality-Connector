package com.example.localityconnector.service;

import com.example.localityconnector.model.Business;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Slf4j
@Service
public class GooglePlacesService {

    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;

    private final String apiKey;
    private final String geocodingApiKey;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String GEOCODING_API_BASE = "https://maps.googleapis.com/maps/api/geocode";

    /**
     * Logs a warning when maps keys are not configured instead of failing startup,
     * since Google Maps integration is optional. API calls will return empty results
     * at request time when keys are absent.
     */
    public GooglePlacesService(
            RestTemplate restTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${google.maps.api.key:}") String apiKey,
            @Value("${google.maps.geocoding.api.key:}") String geocodingApiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Google Maps API key is not configured. Places search will be unavailable.");
        }
        if (geocodingApiKey == null || geocodingApiKey.isBlank()) {
            log.warn("Google Geocoding API key is not configured. Geocoding will be unavailable.");
        }
        this.restTemplate = restTemplate;
        this.passwordEncoder = passwordEncoder;
        this.apiKey = apiKey;
        this.geocodingApiKey = geocodingApiKey;
    }

    @SuppressWarnings("unchecked")
    public List<Business> searchNearbyBusinesses(double latitude, double longitude, int radius, String[] types) {
        List<Business> businesses = new ArrayList<>();

        for (String type : types) {
            try {
                // Use text search for better accuracy and more results
                String searchQuery = buildSearchQuery(type, latitude, longitude);
                String url = UriComponentsBuilder.fromUriString(PLACES_API_BASE + "/textsearch/json")
                        .queryParam("query", searchQuery)
                        .queryParam("location", latitude + "," + longitude)
                        .queryParam("radius", radius)
                        .queryParam("key", apiKey)
                        .build()
                        .toUriString();

                log.info("Searching for {} businesses near {},{} with query: {}", type, latitude, longitude, searchQuery);

                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                Map<String, Object> responseBody = response.getBody();

                if (responseBody != null && responseBody.containsKey("results")) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");

                    for (Map<String, Object> place : results) {
                        Business business = convertPlaceToBusiness(place, type);
                        if (business != null && isValidBusiness(business)) {
                            // Store place_id for enhanced accuracy
                            String placeId = (String) place.get("place_id");
                            if (placeId != null) {
                                enhanceBusinessCoordinatesWithPlaceId(business, placeId);
                            }
                            businesses.add(business);
                        }
                    }
                }

                // Add delay to respect API rate limits
                Thread.sleep(200);

            } catch (Exception e) {
                log.error("Error fetching {} businesses: {}", type, e.getMessage());
            }
        }

        return businesses;
    }

    private String buildSearchQuery(String type, double latitude, double longitude) {
        // Create more specific search queries for better results
        switch (type.toLowerCase()) {
            case "restaurant":
                return "restaurants near me";
            case "pharmacy":
                return "pharmacy medical store near me";
            case "clothing_store":
                return "clothing store fashion near me";
            case "hospital":
                return "hospital medical center near me";
            case "store":
                return "shopping store market near me";
            default:
                return type + " near me";
        }
    }

    private boolean isValidBusiness(Business business) {
        // Validate that business has valid coordinates
        if (business.getLatitude() == null || business.getLongitude() == null) {
            log.warn("Business {} has null coordinates, skipping", business.getBusinessName());
            return false;
        }

        // Validate coordinate ranges for India
        double lat = business.getLatitude();
        double lng = business.getLongitude();

        // India's approximate coordinate bounds
        if (lat < 6.0 || lat > 37.0 || lng < 68.0 || lng > 97.0) {
            log.warn("Business {} has coordinates outside India bounds: {},{}",
                    business.getBusinessName(), lat, lng);
            return false;
        }

        // Check for reasonable precision (at least 4 decimal places); guard against whole-number coordinates
        String latStr = String.valueOf(lat);
        String lngStr = String.valueOf(lng);
        int latDecimals = latStr.contains(".") ? latStr.length() - latStr.indexOf('.') - 1 : 0;
        int lngDecimals = lngStr.contains(".") ? lngStr.length() - lngStr.indexOf('.') - 1 : 0;
        if (latDecimals < 4 || lngDecimals < 4) {
            log.warn("Business {} has low precision coordinates: {},{}",
                    business.getBusinessName(), lat, lng);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private void enhanceBusinessCoordinatesWithPlaceId(Business business, String placeId) {
        try {
            // Get detailed place information for more accurate coordinates
            String detailsUrl = UriComponentsBuilder.fromUriString(PLACES_API_BASE + "/details/json")
                    .queryParam("place_id", placeId)
                    .queryParam("fields", "geometry,formatted_address,address_components")
                    .queryParam("key", apiKey)
                    .build()
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.getForEntity(detailsUrl, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("result")) {
                Map<String, Object> result = (Map<String, Object>) responseBody.get("result");
                Map<String, Object> geometry = (Map<String, Object>) result.get("geometry");

                if (geometry != null) {
                    Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                    if (location != null) {
                        Double newLat = (Double) location.get("lat");
                        Double newLng = (Double) location.get("lng");

                        // Only update if coordinates are more precise
                        if (newLat != null && newLng != null) {
                            business.setLatitude(newLat);
                            business.setLongitude(newLng);
                            log.debug("Enhanced coordinates for {}: {},{} (from place_id: {})",
                                    business.getBusinessName(), newLat, newLng, placeId);
                        }
                    }
                }

                // Update address with more accurate formatted address
                String formattedAddress = (String) result.get("formatted_address");
                if (formattedAddress != null && !formattedAddress.isEmpty()) {
                    business.setAddress(formattedAddress);
                }
            }

            Thread.sleep(150); // Rate limiting

        } catch (Exception e) {
            log.warn("Could not enhance coordinates for {} with place_id {}: {}",
                    business.getBusinessName(), placeId, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Business convertPlaceToBusiness(Map<String, Object> place, String category) {
        try {
            Business business = new Business();

            // Basic info
            business.setBusinessName((String) place.get("name"));
            business.setOwnerName("Google Places Data"); // Default owner name for seeded data
            business.setCategory(mapGoogleTypeToCategory(category));

            // Address
            String address = (String) place.get("vicinity");
            if (address == null || address.isEmpty()) {
                address = "Address not available";
            }
            business.setAddress(address);

            // Phone number
            business.setPhoneNumber("Not available");

            // Location
            Map<String, Object> geometry = (Map<String, Object>) place.get("geometry");
            if (geometry != null) {
                Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                if (location != null) {
                    business.setLatitude((Double) location.get("lat"));
                    business.setLongitude((Double) location.get("lng"));
                }
            }

            // Status
            business.setActive(true);

            // Generate a unique email for the business
            String businessName = business.getBusinessName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            business.setEmail(businessName + "@business.local");

            // Seeded businesses get a random password, BCrypt-hashed so the schema is consistent.
            business.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));

            return business;

        } catch (Exception e) {
            log.error("Error converting place to business: {}", e.getMessage());
            return null;
        }
    }

    private String mapGoogleTypeToCategory(String googleType) {
        switch (googleType.toLowerCase()) {
            case "restaurant":
            case "food":
            case "meal_takeaway":
            case "meal_delivery":
                return "food";
            case "pharmacy":
            case "drugstore":
                return "pharmacy";
            case "clothing_store":
            case "shoe_store":
                return "clothing";
            case "store":
            case "shopping_mall":
                return "stationary";
            case "hospital":
            case "health":
                return "hospital";
            default:
                return "other";
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Double> geocodeAddress(String address) {
        try {
            String url = UriComponentsBuilder.fromUriString(GEOCODING_API_BASE + "/json")
                    .queryParam("address", address)
                    .queryParam("key", geocodingApiKey)
                    .build()
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                if (!results.isEmpty()) {
                    Map<String, Object> result = results.get(0);
                    Map<String, Object> geometry = (Map<String, Object>) result.get("geometry");
                    Map<String, Object> location = (Map<String, Object>) geometry.get("location");

                    Map<String, Double> coordinates = new HashMap<>();
                    coordinates.put("lat", (Double) location.get("lat"));
                    coordinates.put("lng", (Double) location.get("lng"));
                    return coordinates;
                }
            }
        } catch (Exception e) {
            log.error("Error geocoding address {}: {}", address, e.getMessage());
        }

        return null;
    }
}
