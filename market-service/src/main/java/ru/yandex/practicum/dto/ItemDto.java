package ru.yandex.practicum.dto;

import ru.yandex.practicum.entity.Item;
import ru.yandex.practicum.entity.OrderItem;

public record ItemDto(
        long id,
        String title,
        String description,
        String imgPath,
        long price,
        int count
) {
    public ItemDto(Item item, int count) {
        this(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(), item.getPrice(), count);
    }

    public ItemDto(OrderItem item) {
        this(item.getId(), item.getTitle(), null, null, item.getPrice(), item.getCount());
    }
}