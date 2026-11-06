package com.valerii.order.mapper;

import com.valerii.order.model.Order;
import com.valerii.order.model.OrderSummaryDto;
import com.valerii.order.model.entity.OrderEntity;

import java.util.List;

public interface OrderMapper {
    Order toDomain(OrderEntity order);

    List<OrderSummaryDto> toDomainList(List<OrderEntity> orders);
}
