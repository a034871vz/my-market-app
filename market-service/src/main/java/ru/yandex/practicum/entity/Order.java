package ru.yandex.practicum.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("total_sum")
    private Long totalSum;

    public Order(long sum) {
        this.totalSum = sum;
    }
}