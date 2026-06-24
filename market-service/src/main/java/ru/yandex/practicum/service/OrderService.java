package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.entity.Order;
import ru.yandex.practicum.entity.OrderItem;
import ru.yandex.practicum.repository.OrderItemRepository;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;

    @Transactional
    public Mono<Long> createOrder() {
        return cartService.getCartItems()
                .collectList()
                .switchIfEmpty(Mono.error(new RuntimeException("Нет заказов")))
                .flatMap(itemDtos -> {
                    long totalSum = itemDtos.stream()
                            .mapToLong(item -> item.price() * item.count())
                            .sum();
                    return orderRepository.save(new Order(totalSum))
                            .flatMap(order -> {
                                List<OrderItem> orderItems = itemDtos.stream().map(item -> new OrderItem(order.getId(), item)).toList();
                                return orderItemRepository.saveAll(orderItems)
                                        .then(cartService.clearCart())
                                        .thenReturn(order.getId());
                            });
                });
    }

    public Flux<OrderDto> getAllOrders() {
        return orderRepository.findAll()
                .concatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .map(ItemDto::new)
                        .collectList()
                        .map(items -> new OrderDto(order.getId(), items, order.getTotalSum())));
    }

    public Mono<OrderDto> getOrder(Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Заказ не найден: " + id)))
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .map(ItemDto::new)
                        .collectList()
                        .map(items -> new OrderDto(order.getId(), items, order.getTotalSum()))
                );
    }
}