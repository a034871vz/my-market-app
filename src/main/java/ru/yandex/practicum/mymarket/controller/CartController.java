package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.service.CartService;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/cart/items")
    public String getCart(Model model) {
        model.addAttribute("items", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotal());
        return "cart";
    }

    @PostMapping("/cart/items")
    public String updateCart(@RequestParam Long id, @RequestParam String action, Model model) {
        cartService.updateCartItem(id, action);

        model.addAttribute("items", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotal());
        return "cart";
    }
}