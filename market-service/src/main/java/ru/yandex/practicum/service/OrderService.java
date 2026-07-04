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
    public Mono<Long> createOrder(Long userId, Long total) {
        return cartService.getCartItems(userId)
                        .collectList()
                        .switchIfEmpty(Mono.error(new RuntimeException("Нет заказов")))
                        .flatMap(itemDtos -> orderRepository.save(new Order(total, userId))
                                .flatMap(order -> {
                                    List<OrderItem> orderItems = itemDtos.stream().map(item -> new OrderItem(order.getId(), item)).toList();
                                    return orderItemRepository.saveAll(orderItems)
                                            .then(cartService.clearCart(userId))
                                            .thenReturn(order.getId());
                                }));
    }

    public Flux<OrderDto> getAllOrders(Long userId) {
        return orderRepository.findByUserId(userId)
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .map(ItemDto::new)
                        .collectList()
                        .map(items -> new OrderDto(order.getId(), items, order.getTotalSum())));
    }

    public Mono<OrderDto> getOrder(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Заказ не найден: " + orderId)))
                        .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                                .map(ItemDto::new)
                                .collectList()
                                .map(items -> new OrderDto(order.getId(), items, order.getTotalSum()))
                        );
    }
}