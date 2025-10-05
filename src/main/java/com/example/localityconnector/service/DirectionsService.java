package com.example.localityconnector.service;

import com.example.localityconnector.dto.DirectionsRequest;
import com.example.localityconnector.dto.DirectionsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DirectionsService {
    
    private final WebClient mapTilerWebClient;
    
    @Value("${maptiler.api.key}")
    private String mapTilerApiKey;
    
    public Mono<DirectionsResponse> getDirections(DirectionsRequest request) {
        return mapTilerWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/routing/v1/route")
                        .queryParam("key", mapTilerApiKey)
                        .queryParam("start", request.getStartLon() + "," + request.getStartLat())
                        .queryParam("end", request.getEndLon() + "," + request.getEndLat())
                        .queryParam("profile", "driving")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(DirectionsResponse.class)
                .onErrorReturn(createErrorResponse("Failed to get directions"));
    }
    
    public String getDirectionsUrl(DirectionsRequest request) {
        return String.format(
                "https://api.maptiler.com/routing/v1/route?key=%s&start=%s,%s&end=%s,%s&profile=driving",
                mapTilerApiKey,
                request.getStartLon(),
                request.getStartLat(),
                request.getEndLon(),
                request.getEndLat()
        );
    }
    
    public String getMapUrl(double lat, double lon, int zoom) {
        return String.format(
                "https://api.maptiler.com/maps/streets/static/%s,%s/%d/400x300@2x.png?key=%s",
                lon, lat, zoom, mapTilerApiKey
        );
    }
    
    private DirectionsResponse createErrorResponse(String error) {
        DirectionsResponse response = new DirectionsResponse();
        response.setError(error);
        return response;
    }
}



