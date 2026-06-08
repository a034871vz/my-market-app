package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.entity.Item;
import ru.yandex.practicum.repository.CartItemRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.PaymentClientService;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("marketdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/marketdb");
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        registry.add("spring.datasource.url", () ->
                "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/marketdb");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.url", () ->
                "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/marketdb");
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected ItemRepository itemRepository;

    @Autowired
    protected CartItemRepository cartItemRepository;

    @Autowired
    protected CartService cartService;

    @MockitoBean
    protected PaymentClientService paymentClientService;

    protected Long ballId;

    @BeforeEach
    void cleanUp() {
        cartItemRepository.deleteAll().block();
        itemRepository.deleteAll().block();

        Item ball = itemRepository.save(new Item(null, "Мяч", "Описание", "images/ball.png", 1500L)).block();
        this.ballId = ball.getId();

        cartService.updateCartItem(ballId, "PLUS").block();

        Mockito.when(paymentClientService.getBalance()).thenReturn(Mono.just(10000L));
        Mockito.when(paymentClientService.processPayment(Mockito.anyLong()))
                .thenReturn(Mono.just(true));
        Mockito.when(paymentClientService.hasEnoughFunds(Mockito.anyLong()))
                .thenReturn(Mono.just(true));
    }
}