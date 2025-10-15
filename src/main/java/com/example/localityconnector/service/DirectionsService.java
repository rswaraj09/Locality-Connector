package com.example.localityconnector.service;

import com.example.localityconnector.dto.DirectionsRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import com.example.localityconnector.util.GeolocationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DirectionsService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${mappls.client.id:}")
    private String clientId;

    @Value("${mappls.client.secret:}")
    private String clientSecret;

    // For route_adv, Mappls expects REST API key (not OAuth token)
    @Value("${mappls.api.key:}")
    private String restApiKey;

    private final WebClient webClient = WebClient.builder().build();

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
        // Use REST API key for route_adv endpoint
        if (restApiKey == null || restApiKey.isBlank()) {
            result.put("error", "Missing Mappls REST API key");
            return result;
        }

        String routeUrl = String.format(
                "https://apis.mappls.com/advancedmaps/v1/%s/route_adv/driving/%s,%s;%s,%s?geometries=polyline&overview=full",
                urlEncode(restApiKey),
                startLon,
                startLat,
                endLon,
                endLat
        );

        try {
            String body = webClient.get()
                    .uri(routeUrl)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

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
                        boolean near = com.example.localityconnector.util.GeolocationUtils.isWithinRadius(startLat, startLon, endLat, endLon, 0.02);
                        if (near) {
                            result.put("distanceKm", 0.0);
                            result.put("durationMin", 0.0);
                            result.put("note", "No route needed: locations are essentially the same");
                        } else {
                            // Fallback to straight-line distance/time estimate if no route
                            double haversineKm = com.example.localityconnector.util.GeolocationUtils.calculateDistance(startLat, startLon, endLat, endLon);
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

    private String fetchAccessToken() {
        try {
            String tokenUrl = "https://outpost.mappls.com/api/security/oauth/token";
            String form = "grant_type=client_credentials&client_id=" + urlEncode(clientId) + "&client_secret=" + urlEncode(clientSecret);

            String body = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromValue(form))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (body == null) return null;
            JsonNode node = objectMapper.readTree(body);
            return node.path("access_token").asText(null);
        } catch (Exception ex) {
            return null;
        }
    }

    private String urlEncode(String s) {
        return s == null ? "" : URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}



