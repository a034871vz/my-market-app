package ru.yandex.practicum.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.entity.CartItem;

@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {

    Mono<CartItem> findByUserIdAndItemId(Long userId, Long itemId);

    Flux<CartItem> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM cart_items WHERE user_id = :userId")
    Mono<Void> deleteByUserId(Long userId);
}