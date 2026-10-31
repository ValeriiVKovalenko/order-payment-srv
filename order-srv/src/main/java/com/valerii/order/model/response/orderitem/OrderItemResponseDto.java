package com.valerii.order.model.response.orderitem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@Builder
public class OrderItemResponseDto {
    private final Long id;
    private final Long orderId;
    private final String productName;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal lineTotal;
}
