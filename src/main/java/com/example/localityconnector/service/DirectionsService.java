package com.example.localityconnector.service;

import com.example.localityconnector.dto.DirectionsRequest;
import com.example.localityconnector.util.GeolocationUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Mappls-backed routing. The dead {@code fetchAccessToken} OAuth flow (and its unused
 * {@code mappls.client.id}/{@code mappls.client.secret} fields) were removed; {@code route_adv}
 * uses the REST API key directly.
 */
@Slf4j
@Service
public class DirectionsService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    // For route_adv, Mappls expects the REST API key (not an OAuth token).
    private final String restApiKey;

    /**
     * Logs a warning when Mappls key is not configured instead of failing startup,
     * since directions integration is optional.
     */
    public DirectionsService(@Value("${mappls.api.key:}") String restApiKey) {
        if (restApiKey == null || restApiKey.isBlank()) {
            log.warn("Mappls API key is not configured. Directions service will be unavailable.");
        }
        this.restApiKey = restApiKey;
    }

    public String getDirectionsUrl(DirectionsRequest request) {
        return String.format(
                "https://maps.mappls.com/route?sll=%s,%s&dll=%s,%s&rtt=0",
                request.getStartLat(),
                request.getStartLon(),
                request.getEndLat(),
                request.getEndLon()
        );
    }

    public Map<String, Object> getRouteWithMappls(double startLat, double startLon, double endLat, double endLon) {
        Map<String, Object> result = new HashMap<>();

        // Short-circuit when origin and destination are effectively the same (~10 meters)
        try {
            boolean sameSpot = GeolocationUtils.isWithinRadius(startLat, startLon, endLat, endLon, 0.01);
            if (sameSpot) {
                result.put("distanceKm", 0.0);
                result.put("durationMin", 0.0);
                result.put("note", "Origin and destination are the same location");
                result.put("url", getDirectionsUrl(new DirectionsRequest(startLat, startLon, endLat, endLon, "driving")));
                return result;
            }
        } catch (Exception ignored) { }

        String routeUrl = String.format(
                "https://apis.mappls.com/advancedmaps/v1/%s/route_adv/driving/%s,%s;%s,%s?geometries=polyline&overview=full",
                urlEncode(restApiKey),
                startLon,
                startLat,
                endLon,
                endLat
        );

        try {
            String body = restClient.get()
                    .uri(routeUrl)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            if (body != null) {
                JsonNode root = objectMapper.readTree(body);
                JsonNode routes = root.path("routes");
                if (routes.isArray() && routes.size() > 0) {
                    JsonNode first = routes.get(0);
                    double distanceMeters = first.path("distance").asDouble(0);
                    double durationSeconds = first.path("duration").asDouble(0);
                    result.put("distanceKm", distanceMeters / 1000.0);
                    result.put("durationMin", durationSeconds / 60.0);
                } else {
                    // No route found: if points are very close, treat as zero-distance
                    try {
                        boolean near = GeolocationUtils.isWithinRadius(startLat, startLon, endLat, endLon, 0.02);
                        if (near) {
                            result.put("distanceKm", 0.0);
                            result.put("durationMin", 0.0);
                            result.put("note", "No route needed: locations are essentially the same");
                        } else {
                            // Fallback to straight-line distance/time estimate if no route
                            double haversineKm = GeolocationUtils.calculateDistance(startLat, startLon, endLat, endLon);
                            // Simple ETA estimate at 30 km/h average city speed
                            double etaMin = (haversineKm / 30.0) * 60.0;
                            result.put("distanceKm", haversineKm);
                            result.put("durationMin", etaMin);
                            result.put("note", "Approximate values (straight-line fallback). Routing not available for this pair.");
                        }
                    } catch (Exception ignored) {
                        result.put("error", "Route not found");
                    }
                }
            }
        } catch (Exception ex) {
            result.put("error", ex.getMessage());
        }

        // Also include a navigation URL for opening in browser/app
        result.put("url", getDirectionsUrl(new DirectionsRequest(startLat, startLon, endLat, endLon, "driving")));
        return result;
    }

    private String urlEncode(String s) {
        return s == null ? "" : URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
