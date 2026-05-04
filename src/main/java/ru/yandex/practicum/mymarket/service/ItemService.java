package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mymarket.entity.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Page<Item> getItems(String search, String sort, int pageNumber, int pageSize) {
        Sort sorting = Sort.unsorted();
        if ("ALPHA".equals(sort)) {
            sorting = Sort.by("title");
        } else if ("PRICE".equals(sort)) {
            sorting = Sort.by("price");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sorting);

        if (search != null && !search.isEmpty()) {
            return itemRepository.findBySearch(search, pageable);
        }
        return itemRepository.findAll(pageable);
    }

    public Item getItem(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Товар не найден: " + id));
    }
}