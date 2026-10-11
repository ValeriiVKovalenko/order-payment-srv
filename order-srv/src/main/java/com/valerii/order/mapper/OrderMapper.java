package com.valerii.order.mapper;

import com.valerii.order.model.Order;
import com.valerii.order.model.entity.OrderEntity;


public interface OrderMapper {
    Order toDomain(OrderEntity order);

}
