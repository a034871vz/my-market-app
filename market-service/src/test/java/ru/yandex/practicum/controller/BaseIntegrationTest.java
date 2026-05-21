package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.entity.Item;
import ru.yandex.practicum.repository.CartItemRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.service.CartService;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected ItemRepository itemRepository;

    @Autowired
    protected CartItemRepository cartItemRepository;

    @Autowired
    protected CartService cartService;

    protected Long ballId;

    @BeforeEach
    void cleanUp() {
        cartItemRepository.deleteAll().block();
        itemRepository.deleteAll().block();

        Item ball = itemRepository.save(new Item(null, "Мяч", "Описание", "images/ball.png", 1500L)).block();

        this.ballId = ball.getId();

        cartService.updateCartItem(ballId, "PLUS").block();
    }
}