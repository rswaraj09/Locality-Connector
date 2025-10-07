package com.example.localityconnector.service;

import com.example.localityconnector.dto.DirectionsRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
        String accessToken = fetchAccessToken();
        if (accessToken == null || accessToken.isBlank()) {
            result.put("error", "Failed to get Mappls access token");
            return result;
        }

        String routeUrl = String.format(
                "https://apis.mappls.com/advancedmaps/v1/%s/route_adv/driving/%s,%s;%s,%s?geometries=polyline&overview=full",
                urlEncode(accessToken),
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



