package com.example.localityconnector.controller;

import com.example.localityconnector.model.Notification;
import com.example.localityconnector.security.JwtFilter;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.security.SecurityConfig;
import com.example.localityconnector.service.JwtBlacklistService;
import com.example.localityconnector.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NotificationService notificationService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;

    @Test
    void getNotifications_asUser_returns200() throws Exception {
        Notification notif = new Notification();
        notif.setId("n1");
        notif.setTitle("Test Notification");
        when(notificationService.getNotifications("u1")).thenReturn(List.of(notif));

        mockMvc.perform(get("/api/notifications").with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("n1"));
    }

    @Test
    void getUnreadCount_asUser_returnsCount() throws Exception {
        when(notificationService.getUnreadCount("u1")).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/unread-count").with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(5));
    }

    @Test
    void markAsRead_returns200() throws Exception {
        mockMvc.perform(put("/api/notifications/n1/read").with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        verify(notificationService).markAsRead("n1");
    }

    @Test
    void markAllAsRead_returns200() throws Exception {
        when(notificationService.markAllAsRead("u1")).thenReturn(3);

        mockMvc.perform(put("/api/notifications/read-all").with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Marked 3 notifications as read"));
    }

    @Test
    void deleteNotification_whenSuccess_returns200() throws Exception {
        when(notificationService.deleteNotification("n1", "u1")).thenReturn(true);

        mockMvc.perform(delete("/api/notifications/n1").with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteNotification_whenNotFound_returns404() throws Exception {
        when(notificationService.deleteNotification("n1", "u1")).thenReturn(false);

        mockMvc.perform(delete("/api/notifications/n1").with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void clearAllNotifications_returns200() throws Exception {
        when(notificationService.deleteAllNotifications("u1")).thenReturn(10);

        mockMvc.perform(delete("/api/notifications/clear-all").with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Deleted 10 notifications"));
    }
}
