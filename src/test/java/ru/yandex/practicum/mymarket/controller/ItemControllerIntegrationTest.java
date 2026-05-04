package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ItemControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldReturnItemsPage() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));
    }

    @Test
    void shouldReturnItemsPageWithParameters() throws Exception {
        mockMvc.perform(get("/items?search=ball&sort=ALPHA&pageNumber=1&pageSize=2"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));
    }

    @Test
    void shouldAddItemToCart() throws Exception {
        mockMvc.perform(post("/items?id=" + ballId + "&action=PLUS&pageNumber=1&pageSize=5"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void shouldReturnItemPage() throws Exception {
        mockMvc.perform(get("/items/" + ballId))
                .andExpect(status().isOk())
                .andExpect(view().name("item"));
    }
}