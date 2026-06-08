package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
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
    void getBalanceReturnsBalance() {
        webTestClient.get()
                .uri("/api/balance")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceResponse.class)
                .value(response ->
                        assertEquals(10000, response.getAmount())
                );
    }

    @Test
    void processPaymentWithValidRequest_ReturnsSuccess() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(6000L);

        webTestClient.post()
                .uri("/api/payment")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .value(response -> {
                    assertTrue(response.getSuccess());
                    assertEquals(4000, response.getRemainingBalance());
                });
    }
}