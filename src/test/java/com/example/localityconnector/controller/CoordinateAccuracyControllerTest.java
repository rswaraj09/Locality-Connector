package com.example.localityconnector.controller;

import com.example.localityconnector.repository.BusinessFirestoreRepository;
import com.example.localityconnector.security.JwtFilter;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.security.SecurityConfig;
import com.example.localityconnector.service.CoordinateAccuracyService;
import com.example.localityconnector.service.JwtBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CoordinateAccuracyController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class CoordinateAccuracyControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    com.example.localityconnector.service.BusinessService businessService;
    @MockBean
    CoordinateAccuracyService coordinateAccuracyService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;

    @Test
    void validateAll_asAdmin_returns200() throws Exception {
        when(businessService.getAllBusinesses()).thenReturn(List.of());
        when(coordinateAccuracyService.validateAllBusinesses(anyList())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/accuracy/validate-all").with(TestAuth.principal("admin", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void validateAll_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/accuracy/validate-all"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validateBusiness_missing_returns404() throws Exception {
        when(businessService.findById("x")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/accuracy/validate/x").with(TestAuth.principal("admin", "ADMIN")))
                .andExpect(status().isNotFound());
    }
}
