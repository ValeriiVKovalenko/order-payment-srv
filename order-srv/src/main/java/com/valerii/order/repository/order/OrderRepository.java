package com.valerii.order.repository.order;

import com.valerii.order.model.Order;
import com.valerii.order.model.OrderFilterDto;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    OrderEntity saveOrder(Order order);

    void saveOrderItems(List<OrderItem> orderItems,
                        Long orderId);
    Optional<OrderEntity> findById(Long id);

    List<OrderEntity> findAll(OrderFilterDto filter, Integer size, Long offset);

    Long findOrderTotalElements(OrderFilterDto filter);

    String findStatus(Long id);

    void updateStatus(Long id, OrderStatus newOrderStatus);
}
