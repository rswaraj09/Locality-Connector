package com.example.localityconnector.controller;

import com.example.localityconnector.dto.DirectionsRequest;
import com.example.localityconnector.service.DirectionsService;
import com.example.localityconnector.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/directions")
@RequiredArgsConstructor
@Tag(name = "Directions", description = "Routing helpers backed by Mappls")
public class DirectionsController {

    private final DirectionsService directionsService;

    @Operation(summary = "Build a Mappls directions URL between two points")
    @GetMapping("/url")
    public ResponseEntity<ApiResponse<Object>> getDirectionsUrl(
            @RequestParam double startLat,
            @RequestParam double startLon,
            @RequestParam double endLat,
            @RequestParam double endLon) {
        DirectionsRequest request = new DirectionsRequest(startLat, startLon, endLat, endLon, "driving");
        String url = directionsService.getDirectionsUrl(request);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url)));
    }

    @Operation(summary = "Fetch a route via Mappls")
    @GetMapping("/route")
    public ResponseEntity<ApiResponse<Object>> getRoute(
            @RequestParam double startLat,
            @RequestParam double startLon,
            @RequestParam double endLat,
            @RequestParam double endLon) {
        Map<String, Object> data = directionsService.getRouteWithMappls(startLat, startLon, endLat, endLon);
        if (data.containsKey("error")) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(String.valueOf(data.get("error"))));
        }
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
