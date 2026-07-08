package com.example.localityconnector.util;

import ch.hsr.geohash.GeoHash;

import java.util.ArrayList;
import java.util.List;

public class GeolocationUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /** Geohash precision used when persisting business coordinates (~1.2 km cell). */
    public static final int GEOHASH_PRECISION = 6;

    /**
     * Calculate the great-circle distance between two points using the Haversine formula.
     *
     * @return distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    public static boolean isWithinRadius(double userLat, double userLon,
                                         Double businessLat, Double businessLon,
                                         double radiusKm) {
        if (businessLat == null || businessLon == null) {
            return false;
        }
        return calculateDistance(userLat, userLon, businessLat, businessLon) <= radiusKm;
    }

    public static String getFormattedDistance(double userLat, double userLon,
                                              Double businessLat, Double businessLon) {
        if (businessLat == null || businessLon == null) {
            return "Location not available";
        }
        return String.format("%.1f km", calculateDistance(userLat, userLon, businessLat, businessLon));
    }

    // ---------------------------------------------------------------------
    // Geohash helpers (Phase 4)
    // ---------------------------------------------------------------------

    /** Compute the geohash for the given coordinates at the default precision. */
    public static String computeGeohash(double lat, double lng) {
        return GeoHash.withCharacterPrecision(lat, lng, GEOHASH_PRECISION).toBase32();
    }

    public static String computeGeohash(double lat, double lng, int precision) {
        return GeoHash.withCharacterPrecision(lat, lng, precision).toBase32();
    }

    /**
     * Return the geohash of the cell containing the point plus its 8 neighbours, all at
     * the requested precision. Used to build a set of prefix queries that cover a circular
     * search area without missing businesses that sit just across a cell boundary.
     */
    public static List<String> getNeighborGeohashes(double lat, double lng, int precision) {
        GeoHash center = GeoHash.withCharacterPrecision(lat, lng, precision);
        List<String> result = new ArrayList<>();
        result.add(center.toBase32());
        for (GeoHash adjacent : center.getAdjacent()) {
            result.add(adjacent.toBase32());
        }
        return result;
    }

    /** Choose a geohash prefix length appropriate for the search radius (km). */
    public static int precisionForRadiusKm(double radiusKm) {
        if (radiusKm <= 0.6) return 6;   // ~0.6 km
        if (radiusKm <= 2.4) return 5;   // ~2.4 km
        if (radiusKm <= 20) return 4;    // ~20 km
        if (radiusKm <= 78) return 3;    // ~78 km
        return 2;
    }

    // ---------------------------------------------------------------------
    // Indian state detection (Phase 3)
    // ---------------------------------------------------------------------

    /**
     * Best-effort detection of the Indian state containing a coordinate using simple
     * bounding boxes. Karnataka is checked before Maharashtra because their boxes overlap
     * near the shared border and the project prioritises Karnataka.
     *
     * @return the state name, or {@code "Unknown"} if no box matches.
     */
    public static String getIndianState(double lat, double lng) {
        // Karnataka first (priority over Maharashtra on overlapping border regions).
        if (within(lat, lng, 11.5, 18.5, 74.0, 78.6)) return "Karnataka";
        if (within(lat, lng, 15.6, 22.1, 72.6, 80.9)) return "Maharashtra";
        if (within(lat, lng, 8.0, 13.6, 76.2, 80.4)) return "Tamil Nadu";
        if (within(lat, lng, 12.6, 19.9, 76.8, 84.8)) return "Andhra Pradesh";
        if (within(lat, lng, 8.2, 12.8, 74.8, 77.5)) return "Kerala";
        if (within(lat, lng, 27.9, 30.7, 73.8, 78.3)) return "Delhi/NCR";
        if (within(lat, lng, 20.1, 24.7, 68.1, 74.5)) return "Gujarat";
        if (within(lat, lng, 23.8, 30.4, 69.5, 78.3)) return "Rajasthan";
        if (within(lat, lng, 21.1, 26.9, 74.0, 82.8)) return "Madhya Pradesh";
        if (within(lat, lng, 23.9, 31.1, 77.0, 84.7)) return "Uttar Pradesh";
        if (within(lat, lng, 21.6, 27.5, 85.8, 89.9)) return "West Bengal";
        if (within(lat, lng, 17.8, 22.6, 80.2, 84.4)) return "Telangana";
        return "Unknown";
    }

    public static boolean isInState(double lat, double lng, String state) {
        if (state == null) {
            return false;
        }
        return state.equalsIgnoreCase(getIndianState(lat, lng));
    }

    /**
     * Latitude bounds {@code [minLat, maxLat]} for a state's bounding box, mirroring the
     * boxes used by {@link #getIndianState}. Used to push a coarse latitude-range filter
     * into Firestore so {@code by-state} no longer scans the whole collection in memory.
     *
     * @return a 2-element {@code [min, max]} array, or {@code null} for an unknown state.
     */
    public static double[] latitudeBoundsForState(String state) {
        if (state == null) {
            return null;
        }
        switch (state.toLowerCase()) {
            case "karnataka": return new double[]{11.5, 18.5};
            case "maharashtra": return new double[]{15.6, 22.1};
            case "tamil nadu": return new double[]{8.0, 13.6};
            case "andhra pradesh": return new double[]{12.6, 19.9};
            case "kerala": return new double[]{8.2, 12.8};
            case "delhi/ncr": return new double[]{27.9, 30.7};
            case "gujarat": return new double[]{20.1, 24.7};
            case "rajasthan": return new double[]{23.8, 30.4};
            case "madhya pradesh": return new double[]{21.1, 26.9};
            case "uttar pradesh": return new double[]{23.9, 31.1};
            case "west bengal": return new double[]{21.6, 27.5};
            case "telangana": return new double[]{17.8, 22.6};
            default: return null;
        }
    }

    /** Latitude bounds {@code [min, max]} covering mainland India. */
    public static double[] indiaLatitudeBounds() {
        return new double[]{6.0, 37.0};
    }

    private static boolean within(double lat, double lng,
                                  double minLat, double maxLat, double minLng, double maxLng) {
        return lat >= minLat && lat <= maxLat && lng >= minLng && lng <= maxLng;
    }
}
