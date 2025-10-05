package com.example.localityconnector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://maps.googleapis.com/maps/api")
            .build();

    @Value("${google.maps.api.key}")
    private String googleApiKey;

    public Mono<GeocodeResult> geocodeAddress(String address) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/geocode/json")
                        .queryParam("address", address)
                        .queryParam("key", googleApiKey)
                        .build())
                .retrieve()
                .bodyToMono(GeocodeResponse.class)
                .map(resp -> {
                    if (resp == null || resp.results == null || resp.results.length == 0) {
                        return GeocodeResult.error("No results from Google Geocoding API");
                    }
                    GeocodeResponse.Result r = resp.results[0];
                    return GeocodeResult.success(r.geometry.location.lat, r.geometry.location.lng);
                })
                .onErrorReturn(GeocodeResult.error("Failed to geocode address"));
    }

    // Minimal DTOs for Google Geocoding response
    static class GeocodeResponse {
        Result[] results;
        String status;
        static class Result {
            Geometry geometry;
        }
        static class Geometry {
            Location location;
        }
        static class Location {
            double lat;
            double lng;
        }
    }

    public record GeocodeResult(boolean ok, String error, Double lat, Double lon) {
        public static GeocodeResult success(double lat, double lon) {
            return new GeocodeResult(true, null, lat, lon);
        }
        public static GeocodeResult error(String msg) {
            return new GeocodeResult(false, msg, null, null);
        }
    }
}




