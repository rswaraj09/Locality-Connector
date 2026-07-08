package com.example.localityconnector.controller;

import com.example.localityconnector.security.JwtFilter;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.security.SecurityConfig;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.ItemService;
import com.example.localityconnector.service.JwtBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserDashboardController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class UserDashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BusinessService businessService;
    @MockBean
    ItemService itemService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;
    @MockBean
    com.example.localityconnector.service.FeedbackService feedbackService;
    @MockBean
    com.example.localityconnector.service.FavoriteService favoriteService;
    @MockBean
    com.example.localityconnector.service.UserService userService;

    @Test
    void getBusinesses_asUser_returns200() throws Exception {
        when(businessService.getAllBusinesses()).thenReturn(List.of());

        mockMvc.perform(get("/api/user/dashboard/businesses").with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void getBusinesses_asBusiness_returns200() throws Exception {
        when(businessService.getAllBusinesses()).thenReturn(List.of());

        mockMvc.perform(get("/api/user/dashboard/businesses").with(TestAuth.principal("b1", "BUSINESS")))
                .andExpect(status().isOk());
    }

    @Test
    void getBusinesses_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/user/dashboard/businesses"))
                .andExpect(status().isForbidden());
    }

    @Test
    void nearby_missingCoordinates_returns400() throws Exception {
        mockMvc.perform(post("/api/user/dashboard/businesses/nearby").with(TestAuth.principal("u1", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserReviews_returns200() throws Exception {
        when(feedbackService.getFeedbackByUserId("u1")).thenReturn(List.of());
        mockMvc.perform(get("/api/user/dashboard/reviews").with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void getSearchHistory_returns200() throws Exception {
        when(userService.findById("u1")).thenReturn(java.util.Optional.of(new com.example.localityconnector.model.User()));
        mockMvc.perform(get("/api/user/dashboard/history").with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void getNearby_queryParams_returns200() throws Exception {
        when(businessService.getBusinessesWithinRadius(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
        mockMvc.perform(get("/api/user/dashboard/businesses/nearby?latitude=18.5&longitude=73.8&radiusKm=10").with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void getByCategory_returns200() throws Exception {
        when(businessService.getBusinessesByCategory("food")).thenReturn(List.of());
        mockMvc.perform(get("/api/user/dashboard/businesses/category/food").with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk());
    }
}
