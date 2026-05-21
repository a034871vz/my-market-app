package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;

class OrderControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateOrder() {
        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/orders/\\d+\\?newOrder=true");
    }

    @Test
    void shouldReturnOrdersPage() {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(containsString("orders"));
    }

    @Test
    void shouldReturnOrderPage() {
        String location = webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .returnResult(Void.class)
                .getResponseHeaders()
                .getFirst("Location");

        String orderId = location
                .replace("/orders/", "")
                .replace("?newOrder=true", "");

        webTestClient.get()
                .uri("/orders/" + orderId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assert body != null;
                    assert body.contains("order");
                });
    }
}