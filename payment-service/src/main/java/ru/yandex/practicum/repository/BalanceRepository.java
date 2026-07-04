package ru.yandex.practicum.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.entity.Balance;

@Repository
public interface BalanceRepository extends ReactiveCrudRepository<Balance, Long> {

    @Query("SELECT amount FROM payment.balances WHERE user_id = :userId")
    Mono<Long> findAmountByUserId(Long userId);

    @Modifying
    @Query("UPDATE payment.balances SET amount = amount - :amount WHERE user_id = :userId AND amount >= :amount")
    Mono<Integer> deductAmount(Long userId, Long amount);

    @Modifying
    @Query("INSERT INTO payment.balances (user_id, amount) VALUES (:userId, 10000) ON CONFLICT DO NOTHING")
    Mono<Integer> initBalance(Long userId);
}