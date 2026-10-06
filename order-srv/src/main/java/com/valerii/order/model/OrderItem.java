package com.valerii.order.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class OrderItem {
    private final Long id;
    private final Long orderId;
    private final String productName;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal lineTotal;
}
