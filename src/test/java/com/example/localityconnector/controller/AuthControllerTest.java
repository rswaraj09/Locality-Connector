package com.example.localityconnector.controller;

import com.example.localityconnector.model.User;
import com.example.localityconnector.security.JwtFilter;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.security.SecurityConfig;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.JwtBlacklistService;
import com.example.localityconnector.service.LoginAttemptService;
import com.example.localityconnector.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;
    @MockBean
    BusinessService businessService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    LoginAttemptService loginAttemptService;
    @MockBean
    JwtBlacklistService jwtBlacklistService;
    @MockBean
    com.example.localityconnector.service.VerificationService verificationService;
    @MockBean
    com.example.localityconnector.service.PasswordResetService passwordResetService;

    @Test
    void userLogin_happyPath_returnsToken() throws Exception {
        User user = new User();
        user.setId("u1");
        user.setName("Alice");
        user.setEmail("alice@example.com");
        when(loginAttemptService.isLocked(anyString())).thenReturn(false);
        when(userService.login(anyString(), anyString())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(anyString(), anyList(), anyString(), anyString())).thenReturn("token-123");

        mockMvc.perform(post("/api/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@example.com\",\"password\":\"secret1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("token-123"));
    }

    @Test
    void userLogin_invalidCredentials_returns401() throws Exception {
        when(loginAttemptService.isLocked(anyString())).thenReturn(false);
        when(userService.login(anyString(), anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userLogin_malformedBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshToken_returns200() throws Exception {
        when(jwtUtil.extractJti("old-token")).thenReturn("jti-1");
        when(jwtBlacklistService.isBlacklisted("jti-1")).thenReturn(false);
        when(jwtUtil.extractUsername("old-token")).thenReturn("alice@example.com");
        when(jwtUtil.extractRoles("old-token")).thenReturn(java.util.List.of("USER"));
        when(jwtUtil.extractEntityId("old-token")).thenReturn("u1");
        when(jwtUtil.extractEntityName("old-token")).thenReturn("Alice");
        when(jwtUtil.generateToken("alice@example.com", java.util.List.of("USER"), "u1", "Alice")).thenReturn("new-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer old-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("new-token"));
    }
}
