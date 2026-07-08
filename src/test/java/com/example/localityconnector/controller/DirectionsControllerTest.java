package com.example.localityconnector.controller;

import com.example.localityconnector.security.JwtFilter;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.security.SecurityConfig;
import com.example.localityconnector.service.DirectionsService;
import com.example.localityconnector.service.JwtBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DirectionsController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class DirectionsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    DirectionsService directionsService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;

    @Test
    void directionsUrl_isPublic_returns200() throws Exception {
        when(directionsService.getDirectionsUrl(any())).thenReturn("https://maps.mappls.com/route");

        mockMvc.perform(get("/api/directions/url")
                        .param("startLat", "12.9").param("startLon", "77.5")
                        .param("endLat", "13.0").param("endLon", "77.6"))
                .andExpect(status().isOk());
    }

    @Test
    void route_errorResult_returns400() throws Exception {
        when(directionsService.getRouteWithMappls(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Map.<String, Object>of("error", "unreachable"));

        mockMvc.perform(get("/api/directions/route")
                        .param("startLat", "12.9").param("startLon", "77.5")
                        .param("endLat", "13.0").param("endLon", "77.6"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void route_happyPath_returns200() throws Exception {
        when(directionsService.getRouteWithMappls(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Map.<String, Object>of("distanceKm", 1.0));

        mockMvc.perform(get("/api/directions/route")
                        .param("startLat", "12.9").param("startLon", "77.5")
                        .param("endLat", "13.0").param("endLon", "77.6"))
                .andExpect(status().isOk());
    }
}
