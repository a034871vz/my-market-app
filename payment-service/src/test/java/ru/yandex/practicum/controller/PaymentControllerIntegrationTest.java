package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureWebTestClient
class PaymentControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @WithMockUser(authorities = {"SCOPE_payment"})
    void getBalanceReturnsBalance() {
        webTestClient.get()
                .uri("/api/balance?userId=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceResponse.class)
                .value(response ->
                        assertEquals(10000, response.getAmount())
                );
    }

    @Test
    @WithMockUser(authorities = {"SCOPE_payment"})
    void processPaymentWithValidRequest_ReturnsSuccess() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(6000L);

        webTestClient.post()
                .uri("/api/payment?userId=1")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .value(response -> {
                    assertTrue(response.getSuccess());
                    assertEquals(4000, response.getRemainingBalance());
                });
    }

    @Test
    void getBalanceWithoutToken_Returns401() {
        webTestClient.get()
                .uri("/api/balance?userId=1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void processPaymentWithoutToken_Returns401() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(1000L);

        webTestClient.post()
                .uri("/api/payment?userId=1")
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}