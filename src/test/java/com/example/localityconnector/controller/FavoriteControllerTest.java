package com.example.localityconnector.controller;

import com.example.localityconnector.model.Favorite;
import com.example.localityconnector.security.JwtFilter;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.security.SecurityConfig;
import com.example.localityconnector.service.FavoriteService;
import com.example.localityconnector.service.JwtBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FavoriteController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class FavoriteControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FavoriteService favoriteService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;

    @Test
    void addFavorite_returns201() throws Exception {
        Favorite fav = new Favorite();
        fav.setId("fav1");
        when(favoriteService.addFavorite(anyString(), anyString())).thenReturn(fav);

        mockMvc.perform(post("/api/favorites/b1")
                        .with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isCreated());
    }

    @Test
    void removeFavorite_returns200() throws Exception {
        mockMvc.perform(delete("/api/favorites/b1")
                        .with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void listFavorites_returns200() throws Exception {
        when(favoriteService.getUserFavorites("u1")).thenReturn(List.of());

        mockMvc.perform(get("/api/favorites")
                        .with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void listFavorites_paginated_returns200() throws Exception {
        when(favoriteService.getUserFavoritesPaginated("u1", 0, 10)).thenReturn(Map.of());

        mockMvc.perform(get("/api/favorites?page=0&size=10")
                        .with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void toggleFavorite_returns200() throws Exception {
        when(favoriteService.toggleFavorite("u1", "b1")).thenReturn(true);

        mockMvc.perform(post("/api/favorites/b1/toggle")
                        .with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void checkStatus_returns200() throws Exception {
        when(favoriteService.isFavorited("u1", "b1")).thenReturn(true);

        mockMvc.perform(get("/api/favorites/b1/status")
                        .with(TestAuth.principal("u1", "USER")))
                .andExpect(status().isOk());
    }
}
