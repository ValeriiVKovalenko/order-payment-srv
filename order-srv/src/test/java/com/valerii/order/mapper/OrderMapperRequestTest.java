package com.valerii.order.mapper;

import com.valerii.order.model.CreateOrderCommand;
import com.valerii.order.model.request.OrderItemRequestDto;
import com.valerii.order.model.request.OrderRequestDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderMapperRequestTest {
    private final OrderRequestMapper orderRequestMapper = new OrderRequestMapperImpl();

    @Test
    void should_correctlyMapFromRequestToCommand() {
        Long customerId = 101L;
        String currency = "USD";
        OrderRequestDto orderRequest = getOrderRequest(customerId, currency ,
                List.of(getOrderItemRequest("Tea", 2, BigDecimal.valueOf(13.44)),
                        getOrderItemRequest("Coffee", 1, BigDecimal.valueOf(26.38))));

        CreateOrderCommand createOrderCommand = orderRequestMapper.toCreateOrderCommand(orderRequest);
        assertNotNull(createOrderCommand);
        assertEquals(customerId, createOrderCommand.getCustomerId());
        assertEquals(currency, createOrderCommand.getCurrency());

        assertNotNull(orderRequest.getOrderItems());
        assertFalse(orderRequest.getOrderItems().isEmpty());

        OrderItemRequestDto teaOrderItem = orderRequest.getOrderItems().stream()
                .filter(item -> "Tea".equals(item.getProductName()))
                .findFirst()
                .orElseThrow();

        assertNotNull(teaOrderItem);
        assertEquals(2, teaOrderItem.getQuantity());
        assertEquals(0, BigDecimal.valueOf(13.44).compareTo(teaOrderItem.getUnitPrice()));

        OrderItemRequestDto coffeeOrderItem = orderRequest.getOrderItems().stream()
                .filter(item -> "Coffee".equals(item.getProductName()))
                .findFirst()
                .orElseThrow();

        assertNotNull(coffeeOrderItem);
        assertEquals(1, coffeeOrderItem.getQuantity());
        assertEquals(0, BigDecimal.valueOf(26.38).compareTo(coffeeOrderItem .getUnitPrice()));
    }

    private OrderRequestDto getOrderRequest(Long customerId, String currency,
                                            List<OrderItemRequestDto> itemRequestDtoList) {
        return OrderRequestDto.builder()
               .customerId(customerId)
               .currency(currency)
               .orderItems(itemRequestDtoList)
               .build();
    }

    private OrderItemRequestDto getOrderItemRequest(String productName, Integer quantity,
                                                    BigDecimal unitPrice) {
        return OrderItemRequestDto.builder()
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();
    }
}
