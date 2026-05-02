package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class CartControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldReturnCartPage() throws Exception {
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items", "total"));
    }

    @Test
    void shouldUpdateCartItem() throws Exception {
        mockMvc.perform(post("/cart/items?id=" + ballId + "&action=PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));
    }

    @Test
    void shouldDeleteCartItem() throws Exception {
        mockMvc.perform(post("/cart/items?id=" + ballId + "&action=DELETE"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));
    }
}