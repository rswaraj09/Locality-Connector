package com.example.localityconnector.controller;

import com.example.localityconnector.repository.BusinessFirestoreRepository;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BusinessDataController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class BusinessDataControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    com.example.localityconnector.service.BusinessService businessService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;

    @Test
    void all_isPublic_returns200() throws Exception {
        when(businessService.countAll()).thenReturn(0L);
        when(businessService.findAllSorted(anyString(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/business-data/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void search_isPublic_returns200() throws Exception {
        when(businessService.countByNamePrefix(anyString())).thenReturn(0L);
        when(businessService.searchByNamePrefix(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/business-data/search").param("query", "cafe"))
                .andExpect(status().isOk());
    }

    @Test
    void export_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/business-data/export"))
                .andExpect(status().isForbidden());
    }

    @Test
    void export_nonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/api/business-data/export").with(TestAuth.principal("b1", "BUSINESS")))
                .andExpect(status().isForbidden());
    }

    @Test
    void export_asAdmin_returns200() throws Exception {
        when(businessService.getAllBusinesses()).thenReturn(List.of());

        mockMvc.perform(get("/api/business-data/export").with(TestAuth.principal("admin", "ADMIN")))
                .andExpect(status().isOk());
    }
}
