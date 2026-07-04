package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.OrderService;
import ru.yandex.practicum.service.PaymentClientService;
import ru.yandex.practicum.service.UserService;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;
    private final PaymentClientService paymentClientService;

    @PostMapping("/buy")
    public Mono<String> buy() {
        return userService.getCurrentUserId()
                .flatMap(userId -> cartService.getTotal(userId)
                        .flatMap(total -> paymentClientService.processPayment(userId, total)
                                .flatMap(success -> {
                                    if (!success) {
                                        return Mono.just("redirect:/cart/items?error=payment");
                                    }
                                    return orderService.createOrder(userId, total)
                                            .map(orderId -> "redirect:/orders/" + orderId + "?newOrder=true");
                                })));
    }

    @GetMapping("/orders")
    public Mono<String> getOrders(Model model) {
        return userService.getCurrentUserId()
                .flatMap(userId -> orderService.getAllOrders(userId)
                .collectList()
                .map(orders -> {
                    model.addAttribute("orders", orders);
                    return "orders";
                }));
    }

    @GetMapping("/orders/{orderId}")
    public Mono<String> getOrder(@PathVariable Long orderId, @RequestParam(required = false, defaultValue = "false") boolean newOrder, Model model) {
        return userService.getCurrentUserId()
                .flatMap(userId -> orderService.getOrder(orderId, userId)
                .map(orderDto -> {
                    model.addAttribute("order", orderDto);
                    model.addAttribute("newOrder", newOrder);
                    return "order";
                }));
    }
}