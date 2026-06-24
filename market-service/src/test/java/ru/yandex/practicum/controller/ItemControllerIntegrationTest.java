package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ItemControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldReturnItemsPage() {
        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assert body != null;
                    assert body.contains("Витрина магазина");
                });
    }

    @Test
    void shouldReturnItemsPageWithParameters() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("search", "ball")
                        .queryParam("sort", "ALPHA")
                        .queryParam("pageNumber", 1)
                        .queryParam("pageSize", 2)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }

    @Test
    void shouldAddItemToCart() {
        webTestClient.post()
                .uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("id=" + ballId + "&action=PLUS&pageNumber=1&pageSize=5")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    void shouldReturnItemPage() {
        webTestClient.get()
                .uri("/items/" + ballId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assert body != null;
                    assert body.contains("Мяч");
                });
    }
}