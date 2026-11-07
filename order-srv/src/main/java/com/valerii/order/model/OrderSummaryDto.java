package com.valerii.order.model;

import com.valerii.order.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@AllArgsConstructor
@Getter
@Builder
public class OrderSummaryDto {
    private final Long id;
    private final Long customerId;
    private final OrderStatus status;
    private final BigDecimal totalAmount;
    private final String currency;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
}
