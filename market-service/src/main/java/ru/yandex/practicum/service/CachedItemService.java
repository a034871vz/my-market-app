package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.entity.Item;
import ru.yandex.practicum.repository.ItemRepository;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CachedItemService {

    private static final String ITEM_KEY_PREFIX = "item:";
    private static final String PAGE_CONTENT_PREFIX = "items:content:";
    private static final String PAGE_COUNT_PREFIX = "items:count:";
    private static final Duration TTL = Duration.ofMinutes(2);

    private final ReactiveRedisTemplate<String, Item> itemRedisTemplate;
    private final ReactiveRedisTemplate<String, List<Item>> listRedisTemplate;
    private final ReactiveRedisTemplate<String, Long> longRedisTemplate;
    private final ItemRepository itemRepository;

    public Mono<Item> findById(Long id) {
        String key = ITEM_KEY_PREFIX + id;
        return itemRedisTemplate.opsForValue().get(key)
                .switchIfEmpty(
                        itemRepository.findById(id)
                                .flatMap(item -> itemRedisTemplate.opsForValue()
                                        .set(key, item, TTL)
                                        .thenReturn(item))
                );
    }

    public Mono<Page<Item>> findAllPaged(Pageable pageable) {
        String contentKey = pageContentKey(pageable);
        String countKey = pageCountKey("all");

        Mono<List<Item>> cachedContent = listRedisTemplate.opsForValue().get(contentKey);
        Mono<Long> cachedCount = longRedisTemplate.opsForValue().get(countKey);

        return cachedContent.zipWith(cachedCount)
                .map(tuple -> (Page<Item>) new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()))
                .switchIfEmpty(
                        itemRepository.findAllPaged(pageable)
                                .flatMap(page ->
                                        listRedisTemplate.opsForValue().set(contentKey, page.getContent(), TTL)
                                                .then(longRedisTemplate.opsForValue().set(countKey, page.getTotalElements(), TTL))
                                                .thenReturn(page)
                                )
                );
    }

    public Mono<Page<Item>> findBySearch(String search, Pageable pageable) {
        String contentKey = searchContentKey(search, pageable);
        String countKey = searchCountKey(search);

        Mono<List<Item>> cachedContent = listRedisTemplate.opsForValue().get(contentKey);
        Mono<Long> cachedCount = longRedisTemplate.opsForValue().get(countKey);

        return cachedContent.zipWith(cachedCount)
                .map(tuple -> (Page<Item>) new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()))
                .switchIfEmpty(
                        itemRepository.findBySearch(search, pageable)
                                .flatMap(page ->
                                        listRedisTemplate.opsForValue().set(contentKey, page.getContent(), TTL)
                                                .then(longRedisTemplate.opsForValue().set(countKey, page.getTotalElements(), TTL))
                                                .thenReturn(page)
                                )
                );
    }

    private String pageContentKey(Pageable pageable) {
        return String.format("%sall:page=%d:size=%d:sort=%s",
                PAGE_CONTENT_PREFIX, pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().toString().replace(",", ""));
    }
    private String pageCountKey(String prefix) {
        return String.format("%s%s", PAGE_COUNT_PREFIX, prefix);
    }
    private String searchContentKey(String search, Pageable pageable) {
        return String.format("%s%s:page=%d:size=%d:sort=%s",
                PAGE_CONTENT_PREFIX, search, pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().toString().replace(",", ""));
    }
    private String searchCountKey(String search) {
        return String.format("%s%s", PAGE_COUNT_PREFIX, search);
    }
}