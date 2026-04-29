package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.PagingDto;
import ru.yandex.practicum.mymarket.entity.Item;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;

    @GetMapping({"/", "/items"})
    public String getItems(@RequestParam(required = false) String search, @RequestParam(required = false, defaultValue = "NO") String sort,
                           @RequestParam(required = false, defaultValue = "1") int pageNumber, @RequestParam(required = false, defaultValue = "5") int pageSize,
                           Model model) {

        Page<Item> page = itemService.getItems(search, sort, pageNumber, pageSize);
        List<Item> items = page.getContent();

        List<List<ItemDto>> itemRows = new ArrayList<>();
        List<ItemDto> currentRow = new ArrayList<>();

        for (Item item : items) {
            currentRow.add(new ItemDto(
                    item.getId(),
                    item.getTitle(),
                    item.getDescription(),
                    item.getImgPath(),
                    item.getPrice(),
                    cartService.getCount(item.getId())
            ));

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
                pageSize,
                pageNumber,
                page.hasPrevious(),
                page.hasNext()
        ));

        return "items";
    }

    @PostMapping("/items")
    public String updateCartItem(@RequestParam Long id, @RequestParam(required = false) String search,
                                 @RequestParam(required = false, defaultValue = "NO") String sort,
                                 @RequestParam(required = false, defaultValue = "1") int pageNumber,
                                 @RequestParam(required = false, defaultValue = "5") int pageSize, @RequestParam String action) {

        cartService.updateCartItem(id, action);

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
    }
}