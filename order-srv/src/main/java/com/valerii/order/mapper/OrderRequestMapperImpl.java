package com.valerii.order.mapper;

import com.valerii.order.model.CreateOrderCommand;
import com.valerii.order.model.CreateOrderItemCommand;
import com.valerii.order.model.request.OrderItemRequestDto;
import com.valerii.order.model.request.OrderRequestDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderRequestMapperImpl implements OrderRequestMapper {

    @Override
    public CreateOrderCommand toCreateOrderCommand(OrderRequestDto orderRequestDto) {
        List<CreateOrderItemCommand> orderItemList = orderRequestDto.getOrderItems().stream()
                .map(this::toCreateOrderItemCommand)
                .toList();
        return CreateOrderCommand.builder()
                .customerId(orderRequestDto.getCustomerId())
                .currency(orderRequestDto.getCurrency())
                .orderItems(orderItemList)
                .build();
    }

    private CreateOrderItemCommand toCreateOrderItemCommand(OrderItemRequestDto item) {
        return CreateOrderItemCommand.builder()
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build();
    }
}
