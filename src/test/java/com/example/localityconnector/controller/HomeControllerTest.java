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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HomeController only serves Thymeleaf page shells; we assert on the redirect mapping
 * (which needs no template rendering) to keep the slice test independent of template state.
 */
@WebMvcTest(HomeController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class HomeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;

    @Test
    void businessDashboard_isPublicAndRedirects() throws Exception {
        mockMvc.perform(get("/business-dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/enhanced-business-dashboard"));
    }
}
