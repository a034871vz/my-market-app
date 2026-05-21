package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.service.CartService;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/cart/items")
    public Mono<String> getCart(Model model) {
        return Mono.zip(
                cartService.getCartItems().collectList(),
                cartService.getTotal(),
                (items, total) -> {
                    model.addAttribute("items", items);
                    model.addAttribute("total", total);
                    return "cart";
                }
        );
    }

    @PostMapping("/cart/items")
    public Mono<String> updateCart(ServerWebExchange exchange, Model model) {
        return exchange.getFormData()
                .flatMap(formData -> {
                    Long id = Long.valueOf(formData.getFirst("id"));
                    String action = formData.getFirst("action");

                    return cartService.updateCartItem(id, action)
                            .then(cartService.getCartItems().collectList())
                            .zipWith(cartService.getTotal())
                            .doOnNext(tuple -> {
                                model.addAttribute("items", tuple.getT1());
                                model.addAttribute("total", tuple.getT2());
                            })
                            .thenReturn("cart");
                });
    }
}