package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CartControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldReturnCartPage() {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }

    @Test
    void shouldUpdateCartItem() {
        webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("id=" + ballId + "&action=PLUS")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }

    @Test
    void shouldDeleteCartItem() {
        webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("id=" + ballId + "&action=DELETE")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }
}