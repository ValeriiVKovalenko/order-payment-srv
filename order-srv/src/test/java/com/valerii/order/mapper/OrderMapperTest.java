package com.valerii.order.mapper;

import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.entity.OrderItemEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

public class OrderMapperTest {

    private final OrderMapper orderMapper = new OrderMapperImpl();

    @Test
    void should_correctlyMapFromEntityToDomain() {
        Long orderId = 1L;
        OrderItemEntity teeItemEntity = getItemEntity(1L, orderId, "Tee", 2, BigDecimal.valueOf(50),
                BigDecimal.valueOf(100));
        OrderItemEntity coffeeItemEntity = getItemEntity(2L, orderId, "Coffee", 1,
                BigDecimal.valueOf(150), BigDecimal.valueOf(150));

        OrderEntity orderEntity = getOrderEntity(orderId, 101L, BigDecimal.valueOf(250), "USD", "CREATED",
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                List.of(teeItemEntity, coffeeItemEntity));

        Order domain = orderMapper.toDomain(orderEntity);

        // assert order
        assertNotNull(domain);
        assertEquals(orderEntity.getId(), domain.getId());
        assertEquals(orderEntity.getCustomerId(), domain.getCustomerId());
        assertEquals(0, orderEntity.getTotalAmount().compareTo(domain.getTotalAmount()));
        assertEquals(orderEntity.getCurrency(), domain.getCurrency());
        assertEquals(orderEntity.getStatus(), domain.getStatus().name());
        assertEquals(orderEntity.getCreatedAt(), domain.getCreatedAt());
        assertEquals(orderEntity.getUpdatedAt(), domain.getUpdatedAt());

        //assert order items
        assertNotNull(domain.getOrderItems());
        assertFalse(domain.getOrderItems().isEmpty());

        OrderItem teeDomain = domain.getOrderItems().stream()
                .filter(o -> o.getProductName().equals("Tee"))
                .findFirst()
                .orElseThrow();

        assertEquals(teeItemEntity.getId(), teeDomain.getId());
        assertEquals(teeItemEntity.getOrderId(), teeDomain.getOrderId());
        assertEquals(teeItemEntity.getProductName(), teeDomain.getProductName());
        assertEquals(teeItemEntity.getQuantity(), teeDomain.getQuantity());
        assertEquals(0, teeItemEntity.getUnitPrice().compareTo(teeDomain.getUnitPrice()));
        assertEquals(0, teeItemEntity.getLineTotal().compareTo(teeDomain.getLineTotal()));

        OrderItem coffeeDomain = domain.getOrderItems().stream()
                .filter(o -> o.getProductName().equals("Coffee"))
                .findFirst()
                .orElseThrow();

        assertEquals(coffeeItemEntity.getId(), coffeeDomain.getId());
        assertEquals(coffeeItemEntity.getOrderId(), coffeeDomain.getOrderId());
        assertEquals(coffeeItemEntity.getProductName(), coffeeDomain.getProductName());
        assertEquals(coffeeItemEntity.getQuantity(), coffeeDomain.getQuantity());
        assertEquals(0, coffeeItemEntity.getUnitPrice().compareTo(coffeeDomain.getUnitPrice()));
        assertEquals(0, coffeeItemEntity.getLineTotal().compareTo(coffeeDomain.getLineTotal()));

    }

    private OrderItemEntity getItemEntity(Long id, Long orderId, String productName, int quantity,
                                          BigDecimal unitPrice, BigDecimal lineTotal) {
        return OrderItemEntity.builder()
                .id(id)
                .orderId(orderId)
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .lineTotal(lineTotal)
                .build();
    }

    private OrderEntity getOrderEntity(Long orderId, Long customerId, BigDecimal totalAmount, String currency,
                                       String status, OffsetDateTime createdAt, OffsetDateTime updatedAt,
                                       List<OrderItemEntity> orderItemEntities) {

        return OrderEntity.builder()
                .id(orderId)
                .customerId(customerId)
                .totalAmount(totalAmount)
                .currency(currency)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .orderItemsEntity(orderItemEntities)
                .build();
    }

    @Test
    void should_correctlyMapFromEntityToDomainWithoutItemEntity() {
        Long orderId = 1L;
        OrderEntity orderEntity = getOrderEntity(orderId, 101L, BigDecimal.valueOf(250), "USD", "CREATED",
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                emptyList());

        Order domain = orderMapper.toDomain(orderEntity);

        // assert order
        assertNotNull(domain);
        assertEquals(orderEntity.getId(), domain.getId());
        assertEquals(orderEntity.getCustomerId(), domain.getCustomerId());
        assertEquals(0, orderEntity.getTotalAmount().compareTo(domain.getTotalAmount()));
        assertEquals(orderEntity.getCurrency(), domain.getCurrency());
        assertEquals(orderEntity.getStatus(), domain.getStatus().name());
        assertEquals(orderEntity.getCreatedAt(), domain.getCreatedAt());
        assertEquals(orderEntity.getUpdatedAt(), domain.getUpdatedAt());

        //assert order items
        assertNotNull(domain.getOrderItems());
        assertTrue(domain.getOrderItems().isEmpty());
    }
}
