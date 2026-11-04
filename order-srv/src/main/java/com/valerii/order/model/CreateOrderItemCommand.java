package com.valerii.order.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
@RequiredArgsConstructor
@Builder
@Data
public class CreateOrderItemCommand {
    private final String productName;
    private final Integer quantity;
    private final BigDecimal unitPrice;
}
