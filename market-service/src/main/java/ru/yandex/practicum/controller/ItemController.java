package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.dto.PagingDto;
import ru.yandex.practicum.entity.Item;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.ItemService;
import ru.yandex.practicum.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;
    private final UserService userService;

    @GetMapping({"/", "/items"})
    public Mono<String> getItems(@RequestParam(required = false) String search,
                                 @RequestParam(required = false, defaultValue = "NO") String sort,
                                 @RequestParam(required = false, defaultValue = "1") int pageNumber,
                                 @RequestParam(required = false, defaultValue = "5") int pageSize,
                                 Model model) {

        return userService.getCurrentUserId()
                .flatMap(userId -> itemService.getItems(search, sort, pageNumber, pageSize)
                        .flatMap(page -> {
                            List<Item> items = page.getContent();

                            return Flux.fromIterable(items)
                                    .concatMap(item -> cartService.getCount(item.getId(), userId)
                                            .defaultIfEmpty(0)
                                            .map(count -> new ItemDto(item, count)))
                                    .collectList()
                                    .map(itemDtos -> {
                                        List<List<ItemDto>> itemRows = new ArrayList<>();
                                        List<ItemDto> currentRow = new ArrayList<>();
                                        for (ItemDto dto : itemDtos) {
                                            currentRow.add(dto);
                                            if (currentRow.size() == 3) {
                                                itemRows.add(new ArrayList<>(currentRow));
                                                currentRow.clear();
                                            }
                                        }
                                        if (!currentRow.isEmpty()) {
                                            while (currentRow.size() < 3) {
                                                currentRow.add(new ItemDto(-1, "", "", "", 0, 0));
                                            }
                                            itemRows.add(currentRow);
                                        }

                                        model.addAttribute("items", itemRows);
                                        model.addAttribute("search", search != null ? search : "");
                                        model.addAttribute("sort", sort);
                                        model.addAttribute("paging", new PagingDto(
                                                pageSize, pageNumber, page.hasPrevious(), page.hasNext()
                                        ));
                                        return "items";
                                    });
                        }));
    }

    @PostMapping("/items")
    public Mono<String> updateCartItem(ServerWebExchange exchange) {
        return exchange.getFormData()
                .flatMap(formData -> userService.getCurrentUserId()
                        .flatMap(userId -> {
                            Long id = Long.valueOf(formData.getFirst("id"));
                            String action = formData.getFirst("action");
                            String search = formData.getFirst("search");
                            String sort = formData.getFirst("sort");
                            int pageNumber = Integer.parseInt(formData.getFirst("pageNumber"));
                            int pageSize = Integer.parseInt(formData.getFirst("pageSize"));

                            return cartService.updateCartItem(id, action, userId)
                                    .then(Mono.fromCallable(() -> {
                                        StringBuilder redirect = new StringBuilder("redirect:/items?");
                                        redirect.append("pageNumber=").append(pageNumber);
                                        redirect.append("&pageSize=").append(pageSize);
                                        if (search != null && !search.isEmpty()) {
                                            redirect.append("&search=").append(search);
                                        }
                                        if (!"NO".equals(sort)) {
                                            redirect.append("&sort=").append(sort);
                                        }
                                        return redirect.toString();
                                    }));
                        }));
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable Long id, Model model) {
        return userService.getCurrentUserId()
                .flatMap(userId -> itemService.getItem(id)
                        .flatMap(item -> cartService.getCount(item.getId(), userId)
                                .defaultIfEmpty(0)
                                .map(count -> {
                                    model.addAttribute("item", new ItemDto(item, count));
                                    return "item";
                                })));
    }

    @PostMapping("/items/{id}")
    public Mono<String> updateItemCart(@PathVariable Long id, @RequestParam String action, Model model) {
        return userService.getCurrentUserId()
                .flatMap(userId -> cartService.updateCartItem(id, action, userId)
                        .then(itemService.getItem(id))
                        .flatMap(item -> cartService.getCount(item.getId(), userId)
                                .defaultIfEmpty(0)
                                .map(count -> {
                                    model.addAttribute("item", new ItemDto(item, count));
                                    return "item";
                                })));
    }
}