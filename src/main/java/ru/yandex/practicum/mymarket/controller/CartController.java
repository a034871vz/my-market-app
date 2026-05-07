package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public Mono<String> updateCart(@RequestParam Long id, @RequestParam String action, Model model) {
        return cartService.updateCartItem(id, action)
                .then(Mono.zip(
                        cartService.getCartItems().collectList(),
                        cartService.getTotal(),
                        (items, total) -> {
                            model.addAttribute("items", items);
                            model.addAttribute("total", total);
                            return "cart";
                        }
                ));
    }
}