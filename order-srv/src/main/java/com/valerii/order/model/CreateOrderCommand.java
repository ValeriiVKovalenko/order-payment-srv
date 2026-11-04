package com.valerii.order.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
@RequiredArgsConstructor
@Builder
@Data
public class CreateOrderCommand {
    private final Long customerId;
    private final String currency;
    private final List<CreateOrderItemCommand> orderItems;
}
