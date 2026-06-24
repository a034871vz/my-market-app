package ru.yandex.practicum.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.entity.Item;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom {

    private final R2dbcEntityTemplate template;

    @Override
    public Mono<Page<Item>> findBySearch(String search, Pageable pageable) {
        Criteria criteria = buildSearchCriteria(search);

        Mono<Long> count = template.select(Item.class)
                .matching(Query.query(criteria))
                .count();

        Flux<Item> items = template.select(Item.class)
                .matching(
                        Query.query(criteria)
                                .sort(pageable.getSort())
                                .limit(pageable.getPageSize())
                                .offset(pageable.getOffset())
                )
                .all();

        return items.collectList()
                .zipWith(count)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    public Mono<Page<Item>> findAllPaged(Pageable pageable) {
        Mono<Long> count = template.select(Item.class).count();

        Flux<Item> items = template.select(Item.class)
                .matching(
                        Query.empty()
                                .sort(pageable.getSort())
                                .limit(pageable.getPageSize())
                                .offset(pageable.getOffset())
                )
                .all();

        return items.collectList()
                .zipWith(count)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    private Criteria buildSearchCriteria(String search) {
        if (search == null || search.isEmpty()) {
            return Criteria.empty();
        }
        String pattern = "%" + search.toLowerCase() + "%";
        return Criteria.where("title").like(pattern)
                .or("description").like(pattern);
    }
}