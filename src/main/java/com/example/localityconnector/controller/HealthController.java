package com.example.localityconnector.controller;

import com.example.localityconnector.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Liveness + MongoDB connectivity probe. Returns HTTP 503 when MongoDB is unreachable
 * so that orchestrators (Docker/Kubernetes) can detect an unhealthy instance.
 */
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final MongoTemplate mongoTemplate;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("service", "localityconnector");
        try {
            // Ping MongoDB to confirm connectivity
            mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
            data.put("status", "UP");
            data.put("database", "connected");
            return ResponseEntity.ok(ApiResponse.ok(data));
        } catch (Exception e) {
            data.put("status", "DOWN");
            data.put("database", "unavailable");
            data.put("detail", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.fail("MongoDB connectivity check failed"));
        }
    }
}
