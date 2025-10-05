package com.example.localityconnector.controller;

import com.example.localityconnector.dto.DirectionsRequest;
import com.example.localityconnector.dto.DirectionsResponse;
import com.example.localityconnector.service.DirectionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/directions")
@RequiredArgsConstructor
public class DirectionsController {
    
    private final DirectionsService directionsService;
    
    @PostMapping("/route")
    public ResponseEntity<?> getDirections(@RequestBody DirectionsRequest request) {
        try {
            DirectionsResponse response = directionsService.getDirections(request).block();
            if (response.getError() != null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", response.getError()));
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/url")
    public ResponseEntity<?> getDirectionsUrl(
            @RequestParam double startLat,
            @RequestParam double startLon,
            @RequestParam double endLat,
            @RequestParam double endLon) {
        try {
            DirectionsRequest request = new DirectionsRequest(startLat, startLon, endLat, endLon, "driving");
            String url = directionsService.getDirectionsUrl(request);
            return ResponseEntity.ok(java.util.Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/map")
    public ResponseEntity<?> getMapImage(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "15") int zoom) {
        try {
            String mapUrl = directionsService.getMapUrl(lat, lon, zoom);
            return ResponseEntity.ok(java.util.Map.of("mapUrl", mapUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
