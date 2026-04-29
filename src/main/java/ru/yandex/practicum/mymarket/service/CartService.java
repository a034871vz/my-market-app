package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.entity.CartItem;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;

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
        }
    }
}