package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ItemControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldReturnItemsPage_Anonymous() {
        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Витрина магазина");
                    assertThat(body).doesNotContain("Выйти");
                });
    }

    @Test
    void shouldReturnItemsPage_Authenticated() {
        var cookies = webTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=" + testUsername + "&password=" + testPassword)
                .exchange()
                .expectStatus().is3xxRedirection()
                .returnResult(Void.class)
                .getResponseCookies();

        String sessionCookie = cookies.get("SESSION").get(0).getValue();

        webTestClient.get()
                .uri("/items")
                .cookie("SESSION", sessionCookie)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Витрина магазина");
                    assertThat(body).containsAnyOf("Корзина", "Выйти");
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
    void shouldNotAddItemToCart_Anonymous() {
        webTestClient.post()
                .uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("id=" + ballId + "&action=PLUS&pageNumber=1&pageSize=5")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*login.*");
    }

    @Test
    void shouldAddItemToCart_Authenticated() {
        var cookies = webTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=" + testUsername + "&password=" + testPassword)
                .exchange()
                .expectStatus().is3xxRedirection()
                .returnResult(Void.class)
                .getResponseCookies();

        String sessionCookie = cookies.get("SESSION").get(0).getValue();

        webTestClient.post()
                .uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .cookie("SESSION", sessionCookie)
                .bodyValue("id=" + ballId + "&action=PLUS&pageNumber=1&pageSize=5")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/items.*");
    }

    @Test
    void shouldReturnItemPage() {
        webTestClient.get()
                .uri("/items/" + ballId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Мяч"));
    }
}