package com.valerii.order.mapper;

import com.valerii.order.model.CreateOrderCommand;
import com.valerii.order.model.request.OrderRequestDto;

public interface OrderRequestMapper {
    CreateOrderCommand toCreateOrderCommand(OrderRequestDto orderRequestDto);
}
