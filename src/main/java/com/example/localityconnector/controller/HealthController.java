package com.example.localityconnector.controller;

import com.example.localityconnector.util.ApiResponse;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Liveness + Firebase connectivity probe. Returns HTTP 503 when Firestore is unreachable
 * so that orchestrators (Docker/Kubernetes) can detect an unhealthy instance.
 */
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final Firestore firestore;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("service", "localityconnector");
        try {
            // Touch Firestore to confirm connectivity; iterating the collection list is cheap.
            firestore.listCollections().iterator().hasNext();
            data.put("status", "UP");
            data.put("firebase", "connected");
            return ResponseEntity.ok(ApiResponse.ok(data));
        } catch (Exception e) {
            data.put("status", "DOWN");
            data.put("firebase", "unavailable");
            data.put("detail", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.fail("Firebase connectivity check failed"));
        }
    }
}
