package com.valerii.order.model.response.order;

import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.model.response.orderitem.OrderItemResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class OrderResponseDto {
    private final Long id;
    private final Long customerId;
    private final OrderStatus status;
    private final BigDecimal totalAmount;
    private final String currency;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final List<OrderItemResponseDto> orderItems;
}
