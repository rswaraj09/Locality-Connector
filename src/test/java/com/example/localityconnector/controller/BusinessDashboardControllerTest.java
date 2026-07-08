package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.security.JwtFilter;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.security.SecurityConfig;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.FeedbackService;
import com.example.localityconnector.service.JwtBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BusinessDashboardController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class BusinessDashboardControllerTest {

    private static final String LOCATION_BODY =
            "{\"businessLatitude\":12.97,\"businessLongitude\":77.59}";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FeedbackService feedbackService;
    @MockBean
    BusinessService businessService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;
    @MockBean
    com.example.localityconnector.service.ItemService itemService;
    @MockBean
    com.example.localityconnector.service.StorageService storageService;
    @MockBean
    com.example.localityconnector.service.FavoriteService favoriteService;

    @Test
    void getFeedback_happyPath_returns200() throws Exception {
        when(feedbackService.getFeedbackByBusinessId("b1")).thenReturn(List.of());

        mockMvc.perform(get("/api/business/dashboard/feedback").with(TestAuth.principal("b1", "BUSINESS")))
                .andExpect(status().isOk());
    }

    @Test
    void getFeedback_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/business/dashboard/feedback"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFeedback_wrongRole_returns403() throws Exception {
        mockMvc.perform(get("/api/business/dashboard/feedback").with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateLocation_happyPath_returns200() throws Exception {
        Business business = new Business();
        business.setId("b1");
        when(businessService.findById("b1")).thenReturn(Optional.of(business));
        when(businessService.updateLocation(eq("b1"), anyDouble(), anyDouble())).thenReturn(business);

        mockMvc.perform(put("/api/business/dashboard/location").with(TestAuth.principal("b1", "BUSINESS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOCATION_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void updateLocation_missing_returns404() throws Exception {
        when(businessService.findById("b1")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/business/dashboard/location").with(TestAuth.principal("b1", "BUSINESS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOCATION_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateLocation_invalidBody_returns400() throws Exception {
        mockMvc.perform(put("/api/business/dashboard/location").with(TestAuth.principal("b1", "BUSINESS"))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadStorefront_happyPath_returns200() throws Exception {
        Business business = new Business();
        business.setId("b1");
        when(businessService.findById("b1")).thenReturn(Optional.of(business));
        when(storageService.uploadImage(any(), eq("storefronts"))).thenReturn("http://img.png");

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "test.png", "image/png", "data".getBytes());

        mockMvc.perform(multipart("/api/business/dashboard/storefront")
                        .file(file)
                        .with(TestAuth.principal("b1", "BUSINESS")))
                .andExpect(status().isOk());
    }

    @Test
    void deletePhoto_happyPath_returns200() throws Exception {
        Business business = new Business();
        business.setId("b1");
        business.setPhotoUrls(new java.util.ArrayList<>(java.util.List.of("http://img.png")));
        when(businessService.findById("b1")).thenReturn(Optional.of(business));

        mockMvc.perform(delete("/api/business/dashboard/photos?url=http://img.png")
                        .with(TestAuth.principal("b1", "BUSINESS")))
                .andExpect(status().isOk());
    }

    @Test
    void getAnalytics_happyPath_returns200() throws Exception {
        Business business = new Business();
        business.setId("b1");
        business.setBusinessName("Test Biz");
        when(businessService.findById("b1")).thenReturn(Optional.of(business));
        when(feedbackService.getFeedbackByBusinessId("b1")).thenReturn(java.util.Collections.emptyList());
        when(itemService.getItemsByBusinessId("b1")).thenReturn(java.util.Collections.emptyList());
        when(favoriteService.getBusinessFavorites("b1")).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/business/dashboard/analytics")
                        .with(TestAuth.principal("b1", "BUSINESS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.businessName").value("Test Biz"));
    }
}
