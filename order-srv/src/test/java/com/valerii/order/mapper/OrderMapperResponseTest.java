package com.valerii.order.mapper;

import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.model.response.order.OrderResponseDto;
import com.valerii.order.model.response.orderitem.OrderItemResponseDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderMapperResponseTest {
    private final OrderResponseMapper orderResponseMapper = new OrderResponseMapperImpl();

    @Test
    void should_correctlyMapFromDomainToResponse() {
        Long orderId = 1L;
        Long itemTeaId = 1L;
        Long itemCoffeeId = 2L;
        OrderItem teeItem = getOrderItem(itemTeaId, orderId, "Tea", 2, BigDecimal.valueOf(50),
                BigDecimal.valueOf(100));
        OrderItem coffeeItem = getOrderItem(itemCoffeeId, orderId, "Coffee", 1,
                BigDecimal.valueOf(150), BigDecimal.valueOf(150));

        Order domain = getOrder(orderId, 101L, "USD", BigDecimal.valueOf(250), OrderStatus.CREATED,
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                List.of(teeItem, coffeeItem));

        OrderResponseDto result = orderResponseMapper.toResponseDto(domain);

        // assert order
        assertNotNull(result);
        assertEquals(domain.getId(), result.getId());
        assertEquals(domain.getCustomerId(), result.getCustomerId());
        assertEquals(0, domain.getTotalAmount().compareTo(result.getTotalAmount()));
        assertEquals(domain.getCurrency(), result.getCurrency());
        assertEquals(domain.getStatus(), result.getStatus());
        assertEquals(domain.getCreatedAt(), result.getCreatedAt());
        assertEquals(domain.getUpdatedAt(), result.getUpdatedAt());

        //assert order items
        assertNotNull(result.getOrderItems());
        assertFalse(result.getOrderItems().isEmpty());

        OrderItemResponseDto teeResponseDto = result.getOrderItems().stream()
                .filter(o -> o.getProductName().equals("Tea"))
                .findFirst()
                .orElseThrow();

        assertEquals(teeItem.getId(), teeResponseDto.getId());
        assertEquals(teeItem.getOrderId(), teeResponseDto.getOrderId());
        assertEquals(teeItem.getProductName(), teeResponseDto.getProductName());
        assertEquals(teeItem.getQuantity(), teeResponseDto.getQuantity());
        assertEquals(0, teeItem.getUnitPrice().compareTo(teeResponseDto.getUnitPrice()));
        assertEquals(0, teeItem.getLineTotal().compareTo(teeResponseDto.getLineTotal()));

        OrderItemResponseDto coffeeResponseDto = result.getOrderItems().stream()
                .filter(o -> o.getProductName().equals("Coffee"))
                .findFirst()
                .orElseThrow();

        assertEquals(coffeeItem.getId(), coffeeResponseDto.getId());
        assertEquals(coffeeItem.getOrderId(), coffeeResponseDto.getOrderId());
        assertEquals(coffeeItem.getProductName(), coffeeResponseDto.getProductName());
        assertEquals(coffeeItem.getQuantity(), coffeeResponseDto.getQuantity());
        assertEquals(0, coffeeItem.getUnitPrice().compareTo(coffeeResponseDto.getUnitPrice()));
        assertEquals(0, coffeeItem.getLineTotal().compareTo(coffeeResponseDto.getLineTotal()));
    }

    private Order getOrder(Long id, Long customerId, String currency, BigDecimal totalAmount, OrderStatus status,
                           OffsetDateTime createdAt, OffsetDateTime updatedAt,
                           List<OrderItem> orderItems) {
        return Order.builder()
                .id(id)
                .customerId(customerId)
                .currency(currency)
                .totalAmount(totalAmount)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .orderItems(orderItems)
                .build();
    }

    private OrderItem getOrderItem(Long id, Long orderId, String productName, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        return OrderItem.builder()
                .id(id)
                .orderId(orderId)
                .productName(productName)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .build();
    }

    @Test
    void should_correctlyMapFromDomainToResponseWithoutItem() {
        Long orderId = 1L;
        Order domain = getOrder(orderId, 101L, "USD", BigDecimal.valueOf(250), OrderStatus.CREATED,
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                emptyList());

        OrderResponseDto result = orderResponseMapper.toResponseDto(domain);

        // assert order
        assertNotNull(result);
        assertEquals(domain.getId(), result.getId());
        assertEquals(domain.getCustomerId(), result.getCustomerId());
        assertEquals(0, domain.getTotalAmount().compareTo(result.getTotalAmount()));
        assertEquals(domain.getCurrency(), result.getCurrency());
        assertEquals(domain.getStatus(), result.getStatus());
        assertEquals(domain.getCreatedAt(), result.getCreatedAt());
        assertEquals(domain.getUpdatedAt(), result.getUpdatedAt());

        //assert order items
        assertNotNull(result.getOrderItems());
        assertTrue(result.getOrderItems().isEmpty());
    }
}
