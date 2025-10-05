package com.example.localityconnector.util;

public class GeolocationUtils {
    
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    /**
     * Calculate the distance between two points using the Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // Haversine formula
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Check if a business is within the specified radius
     * @param userLat User's latitude
     * @param userLon User's longitude
     * @param businessLat Business latitude
     * @param businessLon Business longitude
     * @param radiusKm Radius in kilometers
     * @return true if business is within radius
     */
    public static boolean isWithinRadius(double userLat, double userLon, 
                                       Double businessLat, Double businessLon, 
                                       double radiusKm) {
        if (businessLat == null || businessLon == null) {
            return false; // Business doesn't have coordinates
        }
        
        double distance = calculateDistance(userLat, userLon, businessLat, businessLon);
        return distance <= radiusKm;
    }
    
    /**
     * Calculate distance and return formatted string
     * @param userLat User's latitude
     * @param userLon User's longitude
     * @param businessLat Business latitude
     * @param businessLon Business longitude
     * @return Formatted distance string (e.g., "2.3 km")
     */
    public static String getFormattedDistance(double userLat, double userLon, 
                                            Double businessLat, Double businessLon) {
        if (businessLat == null || businessLon == null) {
            return "Location not available";
        }
        
        double distance = calculateDistance(userLat, userLon, businessLat, businessLon);
        return String.format("%.1f km", distance);
    }
}
