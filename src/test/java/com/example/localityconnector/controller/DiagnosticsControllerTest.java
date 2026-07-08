package com.example.localityconnector.controller;

import com.example.localityconnector.security.JwtFilter;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.security.SecurityConfig;
import com.example.localityconnector.service.JwtBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiagnosticsController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class DiagnosticsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;

    @Test
    void mapplsEnv_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/diagnostics/mappls-env").with(TestAuth.principal("admin", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void mapplsEnv_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/diagnostics/mappls-env"))
                .andExpect(status().isForbidden());
    }
}
