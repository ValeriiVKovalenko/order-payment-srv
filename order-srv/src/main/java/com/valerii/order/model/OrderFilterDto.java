package com.valerii.order.model;

import com.valerii.order.model.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
@Data
public class OrderFilterDto {
    private final Long customerId;
    private final OrderStatus orderStatus;
}
