package com.valerii.order.mapper;

import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.entity.OrderItemEntity;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.model.response.order.OrderResponseDto;
import com.valerii.order.model.response.orderitem.OrderItemResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public Order toDomain(OrderEntity order) {
        List<OrderItem> orderItemList = order.getOrderItemsEntity().stream()
                .map(this::toOrderItemDomain)
                .toList();
         return Order.builder()
                 .id(order.getId())
                 .customerId(order.getCustomerId())
                 .status(OrderStatus.valueOf(order.getStatus()))
                 .currency(order.getCurrency())
                 .totalAmount(order.getTotalAmount())
                 .createdAt(order.getCreatedAt())
                 .updatedAt(order.getUpdatedAt())
                 .orderItems(orderItemList)
                 .build();
    }

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

    private OrderItem toOrderItemDomain(OrderItemEntity item) {
        return OrderItem.builder()
                .id(item.getId())
                .orderId(item.getOrderId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
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
