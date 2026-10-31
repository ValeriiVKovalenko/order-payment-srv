package com.valerii.order.mapper;

import com.valerii.order.model.Order;
import com.valerii.order.model.response.order.OrderResponseDto;

public interface OrderResponseMapper {
    OrderResponseDto toResponseDto(Order order);
}
