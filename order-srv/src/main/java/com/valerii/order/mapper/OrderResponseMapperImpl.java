package com.valerii.order.mapper;

import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.response.order.OrderResponseDto;
import com.valerii.order.model.response.orderitem.OrderItemResponseDto;

import java.util.List;

public class OrderResponseMapperImpl implements OrderResponseMapper {

    @Override
    public OrderResponseDto toResponseDto(Order order) {
        List<OrderItemResponseDto> orderItemList = order.getOrderItems().stream()
                .map(this::toOrderItemResponseDto)
                .toList();
        return OrderResponseDto.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .currency(order.getCurrency())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(orderItemList)
                .build();
    }

    private OrderItemResponseDto toOrderItemResponseDto(OrderItem item) {
        return OrderItemResponseDto.builder()
                .id(item.getId())
                .orderId(item.getOrderId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
