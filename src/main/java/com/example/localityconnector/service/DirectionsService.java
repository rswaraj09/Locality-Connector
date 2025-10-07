package com.example.localityconnector.service;

import com.example.localityconnector.dto.DirectionsRequest;
import com.example.localityconnector.dto.DirectionsResponse;
import org.springframework.stereotype.Service;

@Service
public class DirectionsService {
    // Mappls does not require server-side request for opening navigation; we construct URL only
    
    public String getDirectionsUrl(DirectionsRequest request) {
        // Use Mappls (MapmyIndia) web route URL for navigation
        // sll = start lat,lon | dll = destination lat,lon | rtt=0 => driving
        return String.format(
                "https://maps.mappls.com/route?sll=%s,%s&dll=%s,%s&rtt=0",
                request.getStartLat(),
                request.getStartLon(),
                request.getEndLat(),
                request.getEndLon()
        );
    }
    
}



