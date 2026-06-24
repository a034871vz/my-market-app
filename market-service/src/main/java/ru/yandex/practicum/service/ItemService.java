package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.entity.Item;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final CachedItemService cachedItemService;

    public Mono<Page<Item>> getItems(String search, String sort, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, buildSort(sort));

        return search != null && !search.isEmpty()
                ? cachedItemService.findBySearch(search, pageable)
                : cachedItemService.findAllPaged(pageable);
    }

    private Sort buildSort(String sort) {
        return switch (sort) {
            case "ALPHA" -> Sort.by("title").ascending();
            case "PRICE" -> Sort.by("price").ascending();
            default -> Sort.unsorted();
        };
    }

    public Mono<Item> getItem(Long id) {
        return cachedItemService.findById(id).switchIfEmpty(Mono.error(() -> new RuntimeException("Товар не найден: " + id)));
    }
}