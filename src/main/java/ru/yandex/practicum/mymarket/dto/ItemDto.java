package ru.yandex.practicum.mymarket.dto;

import ru.yandex.practicum.mymarket.entity.Item;

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
}