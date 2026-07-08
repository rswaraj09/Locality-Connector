package com.example.localityconnector.util;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GeolocationUtilsTest {

    @Test
    void testCalculateDistance() {
        // Bangalore to Mumbai (~840 km)
        double dist = GeolocationUtils.calculateDistance(12.9716, 77.5946, 19.0760, 72.8777);
        assertTrue(dist > 800 && dist < 900, "Distance should be around 840 km");
    }

    @Test
    void testIsWithinRadius() {
        // Same point or very close
        assertTrue(GeolocationUtils.isWithinRadius(12.9716, 77.5946, 12.9720, 77.5950, 1.0));
        assertFalse(GeolocationUtils.isWithinRadius(12.9716, 77.5946, 19.0760, 72.8777, 10.0));
        assertFalse(GeolocationUtils.isWithinRadius(12.9716, 77.5946, null, 77.5950, 10.0));
    }

    @Test
    void testGetFormattedDistance() {
        String formatted = GeolocationUtils.getFormattedDistance(12.9716, 77.5946, 12.9716, 77.5946);
        assertEquals("0.0 km", formatted);
        assertEquals("Location not available", GeolocationUtils.getFormattedDistance(12.9716, 77.5946, null, null));
    }

    @Test
    void testComputeGeohash() {
        String hash = GeolocationUtils.computeGeohash(12.9716, 77.5946);
        assertNotNull(hash);
        assertEquals(6, hash.length());

        String customHash = GeolocationUtils.computeGeohash(12.9716, 77.5946, 4);
        assertEquals(4, customHash.length());
    }

    @Test
    void testGetNeighborGeohashes() {
        List<String> neighbors = GeolocationUtils.getNeighborGeohashes(12.9716, 77.5946, 5);
        assertNotNull(neighbors);
        assertEquals(9, neighbors.size(), "Should include center and 8 neighbors");
    }

    @Test
    void testPrecisionForRadiusKm() {
        assertEquals(6, GeolocationUtils.precisionForRadiusKm(0.5));
        assertEquals(5, GeolocationUtils.precisionForRadiusKm(2.0));
        assertEquals(4, GeolocationUtils.precisionForRadiusKm(15.0));
        assertEquals(3, GeolocationUtils.precisionForRadiusKm(50.0));
        assertEquals(2, GeolocationUtils.precisionForRadiusKm(100.0));
    }

    @Test
    void testGetIndianState() {
        assertEquals("Karnataka", GeolocationUtils.getIndianState(12.9716, 77.5946)); // Bangalore
        assertEquals("Maharashtra", GeolocationUtils.getIndianState(19.0760, 72.8777)); // Mumbai
        assertEquals("Delhi/NCR", GeolocationUtils.getIndianState(28.6139, 77.2090)); // Delhi
        assertEquals("Unknown", GeolocationUtils.getIndianState(0.0, 0.0));
    }

    @Test
    void testIsInState() {
        assertTrue(GeolocationUtils.isInState(12.9716, 77.5946, "Karnataka"));
        assertFalse(GeolocationUtils.isInState(12.9716, 77.5946, "Maharashtra"));
        assertFalse(GeolocationUtils.isInState(12.9716, 77.5946, null));
    }

    @Test
    void testLatitudeBoundsForState() {
        double[] kaBounds = GeolocationUtils.latitudeBoundsForState("Karnataka");
        assertNotNull(kaBounds);
        assertEquals(11.5, kaBounds[0]);
        assertEquals(18.5, kaBounds[1]);

        assertNull(GeolocationUtils.latitudeBoundsForState("UnknownState"));
        assertNull(GeolocationUtils.latitudeBoundsForState(null));
    }

    @Test
    void testIndiaLatitudeBounds() {
        double[] bounds = GeolocationUtils.indiaLatitudeBounds();
        assertNotNull(bounds);
        assertEquals(6.0, bounds[0]);
        assertEquals(37.0, bounds[1]);
    }
}
