package com.valerii.order.service;

import com.valerii.order.mapper.OrderMapper;
import com.valerii.order.model.Order;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements  OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public Order save(Order requestOrder) {
        OrderEntity orderEntity = orderRepository.saveOrder(requestOrder);
        orderRepository.saveOrderItems(requestOrder.getOrderItems(), orderEntity.getId());
        Optional<OrderEntity> order = orderRepository.findById(orderEntity.getId());
        return order.map(orderMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("Order not found after save: " + orderEntity.getId()));
    }
}
