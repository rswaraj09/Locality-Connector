package com.example.localityconnector.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectionsRequest {
    
    private double startLat;
    private double startLon;
    private double endLat;
    private double endLon;
    private String profile = "driving"; // driving, walking, cycling
}



