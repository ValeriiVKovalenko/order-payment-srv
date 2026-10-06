package com.valerii.order.model;

import com.valerii.order.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
public class Order {
    private final Long id;
    private final Long customerId;
    private final OrderStatus status;
    private final BigDecimal totalAmount;
    private final String currency;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final List<OrderItem> orderItems;
}
