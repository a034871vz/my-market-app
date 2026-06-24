package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class MarketServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("marketdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Container
    static GenericContainer<?> paymentService = new GenericContainer<>(
            "my-market-app-payment-service:latest")
            .withExposedPorts(8081);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/marketdb");
        registry.add("spring.datasource.url", () ->
                "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/marketdb");
        registry.add("spring.liquibase.url", () ->
                "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/marketdb");

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);

        registry.add("payment.service.url", () ->
                "http://" + paymentService.getHost() + ":" + paymentService.getFirstMappedPort());
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getItemsPage_ReturnsItems() {
        webTestClient.get()
                .uri("/items?pageNumber=1&pageSize=5")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertNotNull(body, "Body should not be null");
                    assertTrue(body.contains("Витрина магазина"), "Page should contain title");
                });
    }

    @Test
    void addToCart_AndViewCart_Works() {
        webTestClient.post()
                .uri("/cart/items")
                .contentType(APPLICATION_FORM_URLENCODED)
                .bodyValue("id=1&action=PLUS")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertNotNull(body);
                    assertTrue(body.contains("Итого") || body.contains("Купить"));
                });
    }

    @Test
    void checkout_WhenBalanceSufficient_Succeeds() {
        webTestClient.post()
                .uri("/cart/items")
                .contentType(APPLICATION_FORM_URLENCODED)
                .bodyValue("id=5&action=PLUS")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertNotNull(body);
                    assertFalse(body.contains("Недостаточно средств"));
                    assertFalse(body.contains("Сервис платежей недоступен"));
                });

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader()
                .value("Location", location -> {
                    assertNotNull(location);
                    assertTrue(location.matches("/orders/\\d+.*"));
                });
    }

    @Test
    void checkout_WhenBalanceInsufficient_Fails() {
        for (int i = 0; i < 20; i++) {
            webTestClient.post()
                    .uri("/cart/items")
                    .contentType(APPLICATION_FORM_URLENCODED)
                    .bodyValue("id=3&action=PLUS")
                    .exchange()
                    .expectStatus().isOk();
        }

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertNotNull(body);
                    assertTrue(body.contains("Недостаточно средств") ||
                                    body.contains("disabled"));
                });
    }
}