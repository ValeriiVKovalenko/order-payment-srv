package com.valerii.order.mapper;

import com.valerii.order.model.Order;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.response.order.OrderResponseDto;


public interface OrderMapper {
    Order toDomain(OrderEntity order);

    OrderResponseDto toResponseDto(Order order);

}
