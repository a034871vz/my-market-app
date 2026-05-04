package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.entity.Order;
import ru.yandex.practicum.mymarket.entity.OrderItem;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;

    @Transactional
    public Long createOrder() {
        List<ItemDto> cartItems = cartService.getCartItems();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Нет заказов");
        }

        Order order = new Order(cartItems.stream().mapToLong(i -> i.price() * i.count()).sum());
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cartItems.stream().map(item -> new OrderItem(savedOrder.getId(), item)).toList();
        orderItemRepository.saveAll(orderItems);
        cartService.clearCart();
        return savedOrder.getId();
    }

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> {
                    List<ItemDto> items = orderItemRepository.findByOrderId(order.getId()).stream().map(ItemDto::new).toList();
                    return new OrderDto(order.getId(), items, order.getTotalSum());
                })
                .toList();
    }

    public OrderDto getOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Заказ не найден: " + id));
        List<ItemDto> items = orderItemRepository.findByOrderId(order.getId()).stream().map(ItemDto::new).toList();
        return new OrderDto(order.getId(), items, order.getTotalSum());
    }
}