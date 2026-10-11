package com.valerii.order.model.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class OrderEntity {
    private final Long id;
    private final Long customerId;
    private final String status;
    private final BigDecimal totalAmount;
    private final String currency;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final List<OrderItemEntity> orderItemsEntity;
}
