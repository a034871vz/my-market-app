package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.service.OrderService;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy")
    public Mono<String> buy() {
        return orderService.createOrder()
                .map(orderId -> "redirect:/orders/" + orderId + "?newOrder=true");
    }

    @GetMapping("/orders")
    public Mono<String> getOrders(Model model) {
        return orderService.getAllOrders()
                .collectList()
                .map(orders -> {
                    model.addAttribute("orders", orders);
                    return "orders";
                });
    }

    @GetMapping("/orders/{id}")
    public Mono<String> getOrder(@PathVariable Long id, @RequestParam(required = false, defaultValue = "false") boolean newOrder, Model model) {
        return orderService.getOrder(id)
                .map(orderDto -> {
                    model.addAttribute("order", orderDto);
                    model.addAttribute("newOrder", newOrder);
                    return "order";
                });
    }
}