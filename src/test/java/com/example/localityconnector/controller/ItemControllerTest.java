package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.Item;
import com.example.localityconnector.repository.BusinessFirestoreRepository;
import com.example.localityconnector.repository.ItemFirestoreRepository;
import com.example.localityconnector.security.JwtFilter;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.security.SecurityConfig;
import com.example.localityconnector.service.JwtBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class ItemControllerTest {

    private static final String VALID_BODY =
            "{\"itemName\":\"Coffee\",\"itemPrice\":120.0,\"itemDescription\":\"Hot\"}";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    com.example.localityconnector.service.ItemService itemService;
    @MockBean
    com.example.localityconnector.service.BusinessService businessService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtBlacklistService jwtBlacklistService;
    @MockBean
    com.example.localityconnector.service.StorageService storageService;

    @Test
    void create_happyPath_returns201() throws Exception {
        Business business = new Business();
        business.setId("b1");
        business.setBusinessName("Cafe");
        when(businessService.findById("b1")).thenReturn(Optional.of(business));
        when(itemService.createItem(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId("i1");
            return item;
        });

        mockMvc.perform(post("/api/items").with(TestAuth.principal("b1", "BUSINESS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void create_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_wrongRole_returns403() throws Exception {
        mockMvc.perform(post("/api/items").with(TestAuth.principal("u1", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_ownershipViolation_returns403() throws Exception {
        Item item = new Item();
        item.setId("i1");
        item.setBusinessId("b2");
        when(itemService.findById("i1")).thenReturn(Optional.of(item));

        mockMvc.perform(put("/api/items/i1").with(TestAuth.principal("b1", "BUSINESS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_missingItem_returns404() throws Exception {
        when(itemService.findById("i1")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/items/i1").with(TestAuth.principal("b1", "BUSINESS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_invalidBody_returns400() throws Exception {
        mockMvc.perform(put("/api/items/i1").with(TestAuth.principal("b1", "BUSINESS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemName\":\"\",\"itemPrice\":10.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStock_returns200() throws Exception {
        Item item = new Item();
        item.setId("i1");
        item.setBusinessId("b1");
        when(itemService.findById("i1")).thenReturn(Optional.of(item));

        mockMvc.perform(put("/api/items/i1/stock?stock=50")
                        .with(TestAuth.principal("b1", "BUSINESS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock").value(50));
    }

    @Test
    void updateAvailability_returns200() throws Exception {
        Item item = new Item();
        item.setId("i1");
        item.setBusinessId("b1");
        when(itemService.findById("i1")).thenReturn(Optional.of(item));

        mockMvc.perform(put("/api/items/i1/availability?available=false")
                        .with(TestAuth.principal("b1", "BUSINESS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(false));
    }

    @Test
    void delete_ownItem_returns200() throws Exception {
        Item item = new Item();
        item.setId("i1");
        item.setBusinessId("b1");
        when(itemService.findById("i1")).thenReturn(Optional.of(item));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/items/i1")
                        .with(TestAuth.principal("b1", "BUSINESS")))
                .andExpect(status().isOk());
    }
}
