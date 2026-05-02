package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class OrderControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateOrder() throws Exception {
        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*?newOrder=true"));
    }

    @Test
    void shouldReturnOrdersPage() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));
    }

    @Test
    void shouldReturnOrderPage() throws Exception {
        String redirectUrl = mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        String orderId = redirectUrl
                .replace("/orders/", "")
                .replace("?newOrder=true", "");

        mockMvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("order"));
    }
}