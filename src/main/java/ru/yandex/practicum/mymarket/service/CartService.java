package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
}