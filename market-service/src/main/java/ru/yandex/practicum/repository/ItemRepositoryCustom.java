package ru.yandex.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.entity.Item;

public interface ItemRepositoryCustom {
    Mono<Page<Item>> findBySearch(String search, Pageable pageable);

    Mono<Page<Item>> findAllPaged(Pageable pageable);
}