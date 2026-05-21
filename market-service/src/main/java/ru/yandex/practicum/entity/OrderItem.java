package ru.yandex.practicum.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.dto.ItemDto;

@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    private Long id;

    @Column("order_id")
    private Long orderId;

    @Column("item_id")
    private Long itemId;

    @Column()
    private String title;

    @Column()
    private Long price;

    @Column()
    private Integer count;

    public OrderItem(Long orderId, ItemDto item) {
        this.orderId = orderId;
        this.itemId = item.id();
        this.title = item.title();
        this.price = item.price();
        this.count = item.count();
    }
}