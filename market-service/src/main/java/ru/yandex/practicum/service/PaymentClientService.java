package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.client.PaymentApi;
import ru.yandex.practicum.payment.dto.BalanceResponse;
import ru.yandex.practicum.payment.dto.PaymentRequest;
import ru.yandex.practicum.payment.dto.PaymentResponse;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PaymentClientService {

    private final PaymentApi paymentApi;
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    public Mono<Long> getBalance() {
        return paymentApi.getBalance()
                .timeout(TIMEOUT)
                .map(BalanceResponse::getAmount)
                .onErrorReturn(-1L);
    }

    public Mono<Boolean> hasEnoughFunds(long amount) {
        return getBalance()
                .map(balance -> balance >= 0 && balance >= amount);
    }

    public Mono<Boolean> processPayment(long amount) {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(amount);

        return paymentApi.processPayment(request)
                .timeout(TIMEOUT)
                .map(PaymentResponse::getSuccess)
                .onErrorReturn(false);
    }
}