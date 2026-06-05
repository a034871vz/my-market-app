package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.dto.BalanceResponse;
import ru.yandex.practicum.dto.PaymentRequest;
import ru.yandex.practicum.dto.PaymentResponse;
import ru.yandex.practicum.service.PaymentService;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/balance")
    public Mono<BalanceResponse> getBalance() {
        return paymentService.getBalance();
    }

    @PostMapping("/payment")
    public Mono<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        return paymentService.processPayment(request);
    }
}