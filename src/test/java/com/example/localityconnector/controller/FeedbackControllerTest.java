package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.Feedback;
import com.example.localityconnector.model.User;
import com.example.localityconnector.security.JwtFilter;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.security.SecurityConfig;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.FeedbackService;
import com.example.localityconnector.service.JwtBlacklistService;
import com.example.localityconnector.service.UserService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedbackController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class FeedbackControllerTest {

    private static final String VALID_BODY =
            "{\"businessId\":\"b1\",\"rating\":5,\"comment\":\"Great\"}";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FeedbackService feedbackService;
    @MockBean
    BusinessService businessService;
    @MockBean
    UserService userService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;
    @MockBean
    com.example.localityconnector.service.NotificationService notificationService;

    @Test
    void submit_happyPath_returns201() throws Exception {
        Business business = new Business();
        business.setId("b1");
        business.setBusinessName("Cafe");
        when(businessService.findById("b1")).thenReturn(Optional.of(business));
        User user = new User();
        user.setId("u1");
        user.setName("Alice");
        user.setEmail("alice@example.com");
        when(userService.findById("u1")).thenReturn(Optional.of(user));
        Feedback feedback = new Feedback();
        feedback.setId("f1");
        feedback.setRating(5);
        when(feedbackService.submitFeedback(anyString(), any(), anyString(), any(), any(), anyInt(), any()))
                .thenReturn(feedback);

        mockMvc.perform(post("/api/feedback").with(TestAuth.principal("u1", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void submit_wrongRole_returns403() throws Exception {
        mockMvc.perform(post("/api/feedback").with(TestAuth.principal("b1", "BUSINESS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void submit_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void submit_businessNotFound_returns404() throws Exception {
        when(businessService.findById("b1")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/feedback").with(TestAuth.principal("u1", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void submit_invalidBody_returns400() throws Exception {
        mockMvc.perform(post("/api/feedback").with(TestAuth.principal("u1", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":9}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getForBusiness_isPublic_returns200() throws Exception {
        when(feedbackService.getFeedbackByBusinessId("b1")).thenReturn(List.of());

        mockMvc.perform(get("/api/feedback/business/b1"))
                .andExpect(status().isOk());
    }

    @Test
    void edit_ownReview_returns200() throws Exception {
        Feedback f = new Feedback();
        f.setId("f1");
        f.setUserId("u1");
        f.setRating(4);
        when(feedbackService.findById("f1")).thenReturn(Optional.of(f));
        when(feedbackService.updateFeedback(anyString(), anyInt(), any())).thenReturn(f);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/feedback/f1")
                        .with(TestAuth.principal("u1", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessId\":\"b1\",\"rating\":4,\"comment\":\"Updated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_ownReview_returns200() throws Exception {
        Feedback f = new Feedback();
        f.setId("f1");
        f.setUserId("u1");
        when(feedbackService.findById("f1")).thenReturn(Optional.of(f));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/feedback/f1")
                        .with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void report_returns200() throws Exception {
        Feedback f = new Feedback();
        f.setId("f1");
        when(feedbackService.reportFeedback(anyString(), anyString())).thenReturn(f);

        mockMvc.perform(post("/api/feedback/f1/report")
                        .with(TestAuth.principal("u1", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"spam\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getHistogram_returns200() throws Exception {
        when(feedbackService.getRatingHistogram("b1")).thenReturn(java.util.Map.of());

        mockMvc.perform(get("/api/feedback/business/b1/histogram"))
                .andExpect(status().isOk());
    }
}
