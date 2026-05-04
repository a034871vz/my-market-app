package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.entity.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.service.CartService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ItemRepository itemRepository;

    @Autowired
    protected CartItemRepository cartItemRepository;

    @Autowired
    protected CartService cartService;

    protected Long ballId;

    @BeforeEach
    void cleanUp() {
        cartItemRepository.deleteAll();
        itemRepository.deleteAll();

        Item ball = itemRepository.save(new Item(null, "Мяч", "Описание", "images/ball.png", 1500L));

        this.ballId = ball.getId();

        cartService.updateCartItem(ballId, "PLUS");
    }
}