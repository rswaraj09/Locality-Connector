package com.example.localityconnector.controller;

import com.example.localityconnector.security.JwtFilter;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.security.SecurityConfig;
import com.example.localityconnector.service.JwtBlacklistService;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class HealthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    Firestore firestore;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;

    @Test
    void health_firebaseUp_returns200() throws Exception {
        List<CollectionReference> empty = Collections.emptyList();
        when(firestore.listCollections()).thenReturn(empty);

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void health_firebaseDown_returns503() throws Exception {
        when(firestore.listCollections()).thenThrow(new RuntimeException("unavailable"));

        mockMvc.perform(get("/health"))
                .andExpect(status().isServiceUnavailable());
    }
}
