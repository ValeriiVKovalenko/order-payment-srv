package com.valerii.order.service;

import com.valerii.order.model.CreateOrderCommand;
import com.valerii.order.model.Order;

public interface OrderService {
    Order save(CreateOrderCommand order);

    Order findById(Long orderId);
}
