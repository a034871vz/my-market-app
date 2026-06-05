package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.dto.BalanceResponse;
import ru.yandex.practicum.dto.PaymentRequest;
import ru.yandex.practicum.dto.PaymentResponse;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class PaymentService {

    private final AtomicLong balance = new AtomicLong(10000);

    public Mono<BalanceResponse> getBalance() {
        return Mono.just(new BalanceResponse(balance.get()));
    }

    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        long amount = request.amount();
        long currentBalance = balance.get();

        if (currentBalance < amount) {
            return Mono.just(new PaymentResponse(false, currentBalance));
        }

        long newBalance = balance.addAndGet(-amount);
        return Mono.just(new PaymentResponse(true, newBalance));
    }
}