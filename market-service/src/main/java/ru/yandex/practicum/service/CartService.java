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

    public Mono<Integer> getCount(Long itemId, Long userId) {
        return cartItemRepository.findByUserIdAndItemId(userId, itemId)
                        .map(CartItem::getCount)
                        .defaultIfEmpty(0);
    }

    @Transactional
    public Mono<Void> updateCartItem(Long itemId, String action, Long userId) {
        return cartItemRepository.findByUserIdAndItemId(userId, itemId)
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
                        .switchIfEmpty(Mono.defer(() -> {
                            if ("PLUS" .equals(action)) {
                                return cartItemRepository.save(new CartItem(itemId, userId, 1));
                            }
                            return Mono.empty();
                        }))
                        .then();
    }

    public Flux<ItemDto> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .flatMap(cartItem -> cachedItemService.findById(cartItem.getItemId())
                        .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден: " + cartItem.getItemId())))
                        .map(item -> new ItemDto(item, cartItem.getCount())));
    }

    public Mono<Long> getTotal(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .flatMap(cartItem -> cachedItemService.findById(cartItem.getItemId())
                        .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден: " + cartItem.getItemId())))
                        .map(item -> item.getPrice() * cartItem.getCount()))
                .reduce(0L, Long::sum)
                .defaultIfEmpty(0L);
    }

    @Transactional
    public Mono<Void> clearCart(Long userId) {
        return cartItemRepository.deleteByUserId(userId);
    }

    public Mono<Boolean> canCheckout(Long userId) {
        return getTotal(userId).flatMap(total -> paymentClientService.hasEnoughFunds(userId, total));
    }

    public Mono<String> getCheckoutStatusMessage(Long userId) {
        return getTotal(userId)
                .flatMap(total -> paymentClientService.getBalance(userId)
                        .flatMap(balance -> {
                            if (balance < 0) return Mono.just("Сервис платежей недоступен");
                            if (balance < total) return Mono.just("Недостаточно средств на счёте");
                            return Mono.empty();
                        }))
                .defaultIfEmpty("");
    }
}