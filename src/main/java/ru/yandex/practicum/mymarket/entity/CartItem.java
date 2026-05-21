package ru.yandex.practicum.mymarket.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    private Long id;

    @Column("item_id")
    private Long itemId;

    @Column()
    private Integer count;

    public CartItem(Long itemId, int count) {
        this.itemId = itemId;
        this.count = count;
    }
}