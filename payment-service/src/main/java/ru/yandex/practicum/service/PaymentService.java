package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;
import ru.yandex.practicum.repository.BalanceRepository;

import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BalanceRepository balanceRepository;

    public Mono<BalanceResponse> getBalance(Long userId) {
        return balanceRepository.findAmountByUserId(userId)
                .switchIfEmpty(
                        balanceRepository.initBalance(userId)
                                .then(balanceRepository.findAmountByUserId(userId))
                )
                .map(amount -> {
                    BalanceResponse response = new BalanceResponse();
                    response.setAmount(amount);
                    return response;
                });
    }

    @Transactional
    public Mono<PaymentResponse> processPayment(Long userId, PaymentRequest request) {
        long amount = request.getAmount();

        return balanceRepository.deductAmount(userId, amount)
                .flatMap(updated -> {
                    if (updated == 0) {
                        return balanceRepository.findAmountByUserId(userId)
                                .defaultIfEmpty(0L)
                                .map(balance -> {
                                    PaymentResponse response = new PaymentResponse();
                                    response.setSuccess(false);
                                    response.setRemainingBalance(balance);
                                    return response;
                                });
                    }
                    return balanceRepository.findAmountByUserId(userId)
                            .map(balance -> {
                                PaymentResponse response = new PaymentResponse();
                                response.setSuccess(true);
                                response.setRemainingBalance(balance);
                                return response;
                            });
                });
    }
}