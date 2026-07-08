package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.repository.BusinessFirestoreRepository;
import com.example.localityconnector.security.JwtFilter;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.security.SecurityConfig;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.FeedbackService;
import com.example.localityconnector.service.GooglePlacesService;
import com.example.localityconnector.service.ItemService;
import com.example.localityconnector.service.JwtBlacklistService;
import com.example.localityconnector.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BusinessService businessService;
    @MockBean
    UserService userService;
    @MockBean
    ItemService itemService;
    @MockBean
    FeedbackService feedbackService;
    @MockBean
    GooglePlacesService googlePlacesService;
    @MockBean
    BusinessFirestoreRepository businessRepository;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;
    @MockBean
    com.example.localityconnector.service.NotificationService notificationService;

    @Test
    void allBusinesses_asAdmin_returns200() throws Exception {
        when(businessService.getAllBusinesses()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/businesses").with(TestAuth.principal("admin", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void allBusinesses_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/businesses"))
                .andExpect(status().isForbidden());
    }

    @Test
    void allBusinesses_wrongRole_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/businesses").with(TestAuth.principal("b1", "BUSINESS")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBusiness_missing_returns404() throws Exception {
        when(businessService.findById("x")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/admin/businesses/x").with(TestAuth.principal("admin", "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBusiness_happyPath_returns200() throws Exception {
        Business business = new Business();
        business.setId("b1");
        when(businessService.findById("b1")).thenReturn(Optional.of(business));
        when(itemService.deleteItemsByBusinessId("b1")).thenReturn(0);
        when(feedbackService.deleteFeedbackByBusinessId("b1")).thenReturn(0);

        mockMvc.perform(delete("/api/admin/businesses/b1").with(TestAuth.principal("admin", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void platformStatistics_asAdmin_returns200() throws Exception {
        when(businessService.getAllBusinesses()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of());
        when(feedbackService.getAllFeedback()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/statistics").with(TestAuth.principal("admin", "ADMIN")))
                .andExpect(status().isOk());
    }
}
