package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.entity.CartItem;
import ru.yandex.practicum.mymarket.entity.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    public int getCount(Long itemId) {
        return cartItemRepository.findByItemId(itemId)
                .map(CartItem::getCount)
                .orElse(0);
    }

    @Transactional
    public void updateCartItem(Long itemId, String action) {
        CartItem cartItem = cartItemRepository.findByItemId(itemId).orElse(null);

        if ("PLUS".equals(action)) {
            if (cartItem == null) {
                cartItem = new CartItem(itemId, 1);
            } else {
                cartItem.setCount(cartItem.getCount() + 1);
            }
            cartItemRepository.save(cartItem);

        } else if ("MINUS".equals(action)) {
            if (cartItem != null && cartItem.getCount() > 1) {
                cartItem.setCount(cartItem.getCount() - 1);
                cartItemRepository.save(cartItem);
            } else if (cartItem != null) {
                cartItemRepository.delete(cartItem);
            }
        } else if ("DELETE".equals(action)) {
            if (cartItem != null) {
                cartItemRepository.delete(cartItem);
            }
        }
    }

    public List<ItemDto> getCartItems() {
        return cartItemRepository.findAll().stream()
                .map(cartItem -> {
                    Item item = itemRepository.findById(cartItem.getItemId()).orElseThrow(() -> new RuntimeException("Товар не найден: " + cartItem.getItemId()));
                    return new ItemDto(item, cartItem.getCount());
                })
                .toList();
    }

    public long getTotal() {
        return cartItemRepository.findAll().stream()
                .mapToLong(cartItem -> {
                    Item item = itemRepository.findById(cartItem.getItemId()).orElseThrow(() -> new RuntimeException("Товар не найден: " + cartItem.getItemId()));
                    return item.getPrice() * cartItem.getCount();
                })
                .sum();
    }

    @Transactional
    public void clearCart() {
        cartItemRepository.deleteAll();
    }
}