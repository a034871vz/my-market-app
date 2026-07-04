package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CartControllerIntegrationTest extends BaseIntegrationTest {

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
    void shouldRedirectAnonymousFromCart() {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*login.*");
    }

    @Test
    void shouldReturnCartPage_Authenticated() {
        String session = getSessionCookie();

        webTestClient.get()
                .uri("/cart/items")
                .cookie("SESSION", session)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body)
                        .containsAnyOf("Корзина", "Итого", "Пуста"));
    }

    @Test
    void shouldUpdateCartItem_Authenticated() {
        String session = getSessionCookie();

        webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .cookie("SESSION", session)
                .bodyValue("id=" + ballId + "&action=PLUS")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);

        webTestClient.get()
                .uri("/cart/items")
                .cookie("SESSION", session)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body)
                        .containsAnyOf("Мяч", "Итого"));
    }

    @Test
    void shouldDeleteCartItem_Authenticated() {
        String session = getSessionCookie();

        webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .cookie("SESSION", session)
                .bodyValue("id=" + ballId + "&action=PLUS")
                .exchange()
                .expectStatus().isOk();

        webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .cookie("SESSION", session)
                .bodyValue("id=" + ballId + "&action=DELETE")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }

    @Test
    void shouldRedirectAnonymousPostToCart() {
        webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("id=" + ballId + "&action=PLUS")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*login.*");
    }
}