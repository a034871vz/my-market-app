package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.entity.CartItem;
import ru.yandex.practicum.repository.CartItemRepository;
import ru.yandex.practicum.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

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
                .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                        .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден: " + cartItem.getItemId())))
                        .map(item -> new ItemDto(item, cartItem.getCount())));
    }

    public Mono<Long> getTotal() {
        return cartItemRepository.findAll()
                .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId()).switchIfEmpty(Mono.error(new RuntimeException("Товар не найден: " + cartItem.getItemId())))
                        .map(item -> item.getPrice() * cartItem.getCount())
                ).reduce(0L, Long::sum);
    }

    @Transactional
    public Mono<Void> clearCart() {
        return cartItemRepository.deleteAll();
    }
}