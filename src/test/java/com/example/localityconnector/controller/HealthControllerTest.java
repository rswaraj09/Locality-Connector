package com.example.localityconnector.controller;

import com.example.localityconnector.security.JwtFilter;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.security.SecurityConfig;
import com.example.localityconnector.service.JwtBlacklistService;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.servlet.MockMvc;

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
    MongoTemplate mongoTemplate;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;

    @Test
    void health_mongoUp_returns200() throws Exception {
        MongoDatabase mockDb = org.mockito.Mockito.mock(MongoDatabase.class);
        when(mongoTemplate.getDb()).thenReturn(mockDb);
        when(mockDb.runCommand(org.mockito.ArgumentMatchers.any(Document.class)))
                .thenReturn(new Document("ok", 1));

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void health_mongoDown_returns503() throws Exception {
        MongoDatabase mockDb = org.mockito.Mockito.mock(MongoDatabase.class);
        when(mongoTemplate.getDb()).thenReturn(mockDb);
        when(mockDb.runCommand(org.mockito.ArgumentMatchers.any(Document.class)))
                .thenThrow(new RuntimeException("unavailable"));

        mockMvc.perform(get("/health"))
                .andExpect(status().isServiceUnavailable());
    }
}
