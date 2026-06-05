package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.api.PaymentApi;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;
import ru.yandex.practicum.service.PaymentService;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        return paymentService.getBalance().map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> processPayment(Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest.flatMap(paymentService::processPayment).map(ResponseEntity::ok);
    }
}