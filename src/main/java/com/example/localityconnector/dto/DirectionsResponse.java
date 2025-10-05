package com.example.localityconnector.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectionsResponse {
    
    private String error;
    private List<Route> routes;
    private String status;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Route {
        private double distance; // in meters
        private double duration; // in seconds
        private List<Coordinate> geometry;
        private String summary;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordinate {
        private double lat;
        private double lon;
    }
}



