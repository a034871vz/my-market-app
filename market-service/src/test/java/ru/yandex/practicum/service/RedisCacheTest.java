package ru.yandex.practicum.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import ru.yandex.practicum.entity.Item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
class RedisCacheTest {

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
    private CachedItemService cachedItemService;

    @Autowired
    private ReactiveRedisTemplate<String, Item> itemRedisTemplate;

    @BeforeEach
    void setUp() {
        itemRedisTemplate.keys("item:*")
                .flatMap(key -> itemRedisTemplate.delete(key))
                .then()
                .block();
    }

    @Test
    void findById_CacheMiss_LoadsFromDbAndCaches() {
        Boolean existsBefore = itemRedisTemplate.hasKey("item:1").block();
        assertFalse(existsBefore != null && existsBefore);

        StepVerifier.create(cachedItemService.findById(1L))
                .assertNext(item -> {
                    assertNotNull(item);
                    assertEquals(1L, item.getId());
                })
                .verifyComplete();

        Boolean existsAfter = itemRedisTemplate.hasKey("item:1").block();
        assertTrue(existsAfter != null && existsAfter);
    }

    @Test
    void findById_CacheHit_ReturnsFromCache() {
        cachedItemService.findById(1L).block();

        StepVerifier.create(cachedItemService.findById(1L))
                .assertNext(item -> {
                    assertNotNull(item);
                    assertEquals(1L, item.getId());
                })
                .verifyComplete();
    }

    @Test
    void findAllPaged_CacheMiss_LoadsFromDbAndCaches() {
        Pageable pageable = PageRequest.of(0, 5);

        StepVerifier.create(cachedItemService.findAllPaged(pageable))
                .assertNext(page -> {
                    assertNotNull(page);
                    assertFalse(page.getContent().isEmpty());
                })
                .verifyComplete();

        Boolean contentExists = itemRedisTemplate.hasKey("items:content:all:page=0:size=5:sort=UNSORTED").block();
        assertTrue(contentExists != null && contentExists);

        Boolean countExists = itemRedisTemplate.hasKey("items:count:all").block();
        assertTrue(countExists != null && countExists);
    }

    @Test
    void findBySearch_CacheMiss_LoadsFromDbAndCaches() {
        Pageable pageable = PageRequest.of(0, 5);

        StepVerifier.create(cachedItemService.findBySearch("мяч", pageable))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

        String expectedKey = "items:content:search=мяч:page=0:size=5:sort=UNSORTED";

        Boolean exists = itemRedisTemplate.hasKey(expectedKey).block();
        assertTrue(exists != null && exists);
    }
}