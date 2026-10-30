package com.valerii.order.service;

import com.valerii.order.model.Order;
import jakarta.validation.Valid;

public interface OrderService {
    Order save(@Valid Order order);

    Order getById(Long orderId);
}
