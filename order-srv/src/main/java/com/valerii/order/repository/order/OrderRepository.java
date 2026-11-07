package com.valerii.order.repository.order;

import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.request.filter.OrderFilterRequestDto;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    OrderEntity saveOrder(Order order);

    void saveOrderItems(List<OrderItem> orderItems,
                        Long orderId);
    Optional<OrderEntity> findById(Long id);

    List<OrderEntity> findAll(OrderFilterRequestDto filter, Integer size, Long offset);

    Long findOrderTotalElements(OrderFilterRequestDto filter);
}
