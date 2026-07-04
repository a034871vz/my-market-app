package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class OrderControllerIntegrationTest extends BaseIntegrationTest {

    private String getSessionCookie() {
        return webTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=" + testUsername + "&password=" + testPassword)
                .exchange()
                .expectStatus().is3xxRedirection()
                .returnResult(Void.class)
                .getResponseCookies()
                .get("SESSION")
                .get(0)
                .getValue();
    }

    @Test
    void shouldRedirectAnonymousFromBuy() {
        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*login.*");
    }

    @Test
    void shouldRedirectAnonymousFromOrders() {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*login.*");
    }

    @Test
    void shouldCreateOrder_Authenticated() {
        String session = getSessionCookie();

        webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .cookie("SESSION", session)
                .bodyValue("id=" + ballId + "&action=PLUS")
                .exchange()
                .expectStatus().isOk();

        webTestClient.post()
                .uri("/buy")
                .cookie("SESSION", session)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/orders/\\d+\\?newOrder=true");
    }

    @Test
    void shouldReturnOrdersPage_Authenticated() {
        String session = getSessionCookie();

        webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .cookie("SESSION", session)
                .bodyValue("id=" + ballId + "&action=PLUS")
                .exchange()
                .expectStatus().isOk();

        webTestClient.post()
                .uri("/buy")
                .cookie("SESSION", session)
                .exchange()
                .expectStatus().is3xxRedirection();

        webTestClient.get()
                .uri("/orders")
                .cookie("SESSION", session)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Заказ №"));
    }

    @Test
    void shouldReturnOrderPage_Authenticated() {
        String session = getSessionCookie();

        webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .cookie("SESSION", session)
                .bodyValue("id=" + ballId + "&action=PLUS")
                .exchange()
                .expectStatus().isOk();

        String location = webTestClient.post()
                .uri("/buy")
                .cookie("SESSION", session)
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
                .cookie("SESSION", session)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).containsIgnoringCase("заказ"));
    }
}