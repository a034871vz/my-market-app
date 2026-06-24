package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.UserService;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping("/cart/items")
    public Mono<String> getCart(Model model) {
        return userService.getCurrentUserId()
                .flatMap(userId ->  Mono.zip(
                cartService.getCartItems(userId).collectList(),
                cartService.getTotal(userId),
                cartService.canCheckout(userId),
                cartService.getCheckoutStatusMessage(userId)
        ).map(tuple -> {
            model.addAttribute("items", tuple.getT1());
            model.addAttribute("total", tuple.getT2());
            model.addAttribute("canCheckout", tuple.getT3());
            model.addAttribute("checkoutMessage", tuple.getT4());
            return "cart";
        }));
    }

    @PostMapping("/cart/items")
    public Mono<String> updateCart(ServerWebExchange exchange, Model model) {
        return exchange.getFormData()
                .flatMap(formData -> userService.getCurrentUserId()
                        .flatMap(userId -> {
                            Long id = Long.valueOf(formData.getFirst("id"));
                            String action = formData.getFirst("action");

                            return cartService.updateCartItem(id, action, userId)
                                    .then(getCart(model));
                        }));
    }
}