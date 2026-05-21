package ru.yandex.practicum.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.entity.Item;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long>, ItemRepositoryCustom {
}