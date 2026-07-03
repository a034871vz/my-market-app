package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import ru.yandex.practicum.config.TestOAuth2Config;
import ru.yandex.practicum.entity.Item;
import ru.yandex.practicum.entity.User;
import ru.yandex.practicum.repository.CartItemRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.OrderItemRepository;
import ru.yandex.practicum.repository.OrderRepository;
import ru.yandex.practicum.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import(TestOAuth2Config.class)
class MarketServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("marketdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/marketdb");
        registry.add("spring.r2dbc.username", () -> "postgres");
        registry.add("spring.r2dbc.password", () -> "postgres");
        registry.add("spring.datasource.url", () ->
                "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/marketdb");
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
        registry.add("spring.liquibase.url", () ->
                "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/marketdb");
        registry.add("spring.liquibase.user", () -> "postgres");
        registry.add("spring.liquibase.password", () -> "postgres");

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ReactiveRedisConnectionFactory redisConnectionFactory;

    @MockitoBean
    private PaymentClientService paymentClientService;

    private Long ballId;
    private String testUsername = "testuser";
    private String testPassword = "testpass";

    @BeforeEach
    void setUp() {
        redisConnectionFactory.getReactiveConnection()
                .serverCommands()
                .flushAll()
                .block();

        orderItemRepository.deleteAll().block();
        orderRepository.deleteAll().block();
        cartItemRepository.deleteAll().block();
        itemRepository.deleteAll().block();
        userRepository.deleteAll().block();

        User user = new User();
        user.setUsername(testUsername);
        user.setPassword(passwordEncoder.encode(testPassword));
        user.setRole("USER");
        userRepository.save(user).block();

        Item ball = itemRepository.save(new Item(null, "Мяч", "Описание", "images/ball.png", 1500L)).block();
        this.ballId = ball.getId();

        Mockito.reset(paymentClientService);
        Mockito.when(paymentClientService.getBalance(Mockito.anyLong())).thenReturn(Mono.just(10000L));
        Mockito.when(paymentClientService.processPayment(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(Mono.just(true));
        Mockito.when(paymentClientService.hasEnoughFunds(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(Mono.just(true));
    }

    private String getSessionCookie() {
        return webTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=" + testUsername + "&password=" + testPassword)
                .exchange()
                .expectStatus().is3xxRedirection()
                .returnResult(Void.class)
                .getResponseCookies()
                .get("SESSION")
                .get(0)
                .getValue();
    }

    @Test
    void getItemsPage_ReturnsItems() {
        webTestClient.get()
                .uri("/items?pageNumber=1&pageSize=5")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertNotNull(body, "Body should not be null");
                    assertTrue(body.contains("Витрина магазина"), "Page should contain title");
                });
    }

    @Test
    void addToCart_AndViewCart_Works() {
        String session = getSessionCookie();

        webTestClient.post()
                .uri("/cart/items")
                .contentType(APPLICATION_FORM_URLENCODED)
                .cookie("SESSION", session)
                .bodyValue("id=" + ballId + "&action=PLUS")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/cart/items")
                .cookie("SESSION", session)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertNotNull(body);
                    assertTrue(body.contains("Итого") || body.contains("Купить"));
                });
    }

    @Test
    void checkout_WhenBalanceSufficient_Succeeds() {
        String session = getSessionCookie();

        webTestClient.post()
                .uri("/cart/items")
                .contentType(APPLICATION_FORM_URLENCODED)
                .cookie("SESSION", session)
                .bodyValue("id=" + ballId + "&action=PLUS")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/cart/items")
                .cookie("SESSION", session)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertNotNull(body);
                    assertFalse(body.contains("Недостаточно средств"));
                    assertFalse(body.contains("Сервис платежей недоступен"));
                });

        webTestClient.post()
                .uri("/buy")
                .cookie("SESSION", session)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader()
                .value("Location", location -> {
                    assertNotNull(location);
                    assertTrue(location.matches("/orders/\\d+.*"));
                });
    }

    @Test
    void checkout_WhenBalanceInsufficient_Fails() {
        Mockito.when(paymentClientService.hasEnoughFunds(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(Mono.just(false));
        Mockito.when(paymentClientService.getBalance(Mockito.anyLong()))
                .thenReturn(Mono.just(0L));

        String session = getSessionCookie();

        webTestClient.post()
                .uri("/cart/items")
                .contentType(APPLICATION_FORM_URLENCODED)
                .cookie("SESSION", session)
                .bodyValue("id=" + ballId + "&action=PLUS")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/cart/items")
                .cookie("SESSION", session)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertNotNull(body);
                    assertTrue(body.contains("Недостаточно средств") || body.contains("disabled"));
                });
    }
}