package com.example.localityconnector.controller;

import com.example.localityconnector.dto.DirectionsRequest;
import com.example.localityconnector.service.DirectionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/directions")
@RequiredArgsConstructor
public class DirectionsController {
    
    private final DirectionsService directionsService;
    
    // Removed MapTiler route proxy; client uses URL endpoint below for Mappls
    
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
    
    // Removed static map image endpoint (MapTiler-specific)
}
