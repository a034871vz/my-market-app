package ru.yandex.practicum.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("payment.balances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Balance {

    @Id
    private Long userId;
    private Long amount;

    public Balance(Long userId) {
        this.userId = userId;
        this.amount = 10000L;
    }
}