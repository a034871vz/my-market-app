package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.entity.CartItem;
import ru.yandex.practicum.repository.CartItemRepository;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final CachedItemService cachedItemService;
    private final PaymentClientService paymentClientService;

    public Mono<Integer> getCount(Long itemId) {
        return cartItemRepository.findByItemId(itemId)
                .map(CartItem::getCount)
                .defaultIfEmpty(0);
    }

    @Transactional
    public Mono<Void> updateCartItem(Long itemId, String action) {
        return cartItemRepository.findByItemId(itemId)
                .flatMap(cartItem -> switch (action) {
                    case "PLUS" -> {
                        cartItem.setCount(cartItem.getCount() + 1);
                        yield cartItemRepository.save(cartItem);
                    }
                    case "MINUS" -> {
                        if (cartItem.getCount() > 1) {
                            cartItem.setCount(cartItem.getCount() - 1);
                            yield cartItemRepository.save(cartItem);
                        } else {
                            yield cartItemRepository.delete(cartItem);
                        }
                    }
                    case "DELETE" -> cartItemRepository.delete(cartItem);
                    default -> Mono.error(new IllegalArgumentException("Неизвестное действие " + action));
                })
                .switchIfEmpty("PLUS".equals(action)
                        ? cartItemRepository.save(new CartItem(itemId, 1)).then()
                        : Mono.empty()).then();
    }

    public Flux<ItemDto> getCartItems() {
        return cartItemRepository.findAll()
                .flatMap(cartItem -> cachedItemService.findById(cartItem.getItemId())
                        .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден: " + cartItem.getItemId())))
                        .map(item -> new ItemDto(item, cartItem.getCount())));
    }

    public Mono<Long> getTotal() {
        return cartItemRepository.findAll()
                .flatMap(cartItem -> cachedItemService.findById(cartItem.getItemId())
                        .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден: " + cartItem.getItemId())))
                        .map(item -> item.getPrice() * cartItem.getCount()))
                .reduce(0L, Long::sum);
    }

    @Transactional
    public Mono<Void> clearCart() {
        return cartItemRepository.deleteAll();
    }

    public Mono<Boolean> canCheckout() {
        return getTotal().flatMap(paymentClientService::hasEnoughFunds);
    }

    public Mono<String> getCheckoutStatusMessage() {
        return getTotal()
                .flatMap(total -> paymentClientService.getBalance()
                        .flatMap(balance -> {
                            if (balance < 0) return Mono.just("Сервис платежей недоступен");
                            if (balance < total) return Mono.just("Недостаточно средств на счёте");
                            return Mono.empty();
                        }))
                .defaultIfEmpty("");
    }
}