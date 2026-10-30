package com.valerii.order.service;

import com.valerii.order.exception.OrderNotFoundException;
import com.valerii.order.mapper.OrderMapper;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.entity.OrderItemEntity;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.repository.order.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;


    @Test
    void getById_shouldReturnOrder_whenExists() {
        //given
        Long orderId = 100L;
        Long orderItemTeaId = 1L;
        Long orderItemCoffeeId = 2L;
        OrderItemEntity teeItemEntity = getItemEntity(orderItemTeaId, orderId, "Tea", 2, BigDecimal.valueOf(50),
                BigDecimal.valueOf(100));
        OrderItemEntity coffeeItemEntity = getItemEntity(orderItemCoffeeId, orderId, "Coffee", 1,
                BigDecimal.valueOf(150), BigDecimal.valueOf(150));

        OrderEntity orderEntity = getOrderEntity(orderId, 101L, BigDecimal.valueOf(250), "USD", "CREATED",
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                List.of(teeItemEntity, coffeeItemEntity));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        Order orderDomain = getOrder(
                orderId,
                101L,
                "USD",
                BigDecimal.valueOf(250),
                OrderStatus.CREATED,
                List.of(getOrderItem(orderItemTeaId, orderId, "Tea", 2, BigDecimal.valueOf(50),
                                BigDecimal.valueOf(100)),
                        getOrderItem(orderItemCoffeeId, orderId, "Coffee", 1, BigDecimal.valueOf(150),
                                BigDecimal.valueOf(150))));

        when(orderMapper.toDomain(orderEntity)).thenReturn(orderDomain);

        //when
        Order result = orderService.findById(orderId);

        //then
        assertNotNull(result);
        assertSame(orderDomain, result);
        assertNotNull(result.getId());
        assertEquals(orderId, result.getId());

        verify(orderRepository).findById(orderId);
        verify(orderMapper).toDomain(orderEntity);
    }

    @Test
    void getById_shouldReturnNotFound_whenNotFound() {
        //given
        Long orderId = 99L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(orderId))
                .isInstanceOf(OrderNotFoundException.class);
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

    private Order getOrder(Long id, Long customerId, String currency, BigDecimal totalAmount, OrderStatus status,
                           List<OrderItem> orderItems) {
        return Order.builder()
                .id(id)
                .customerId(customerId)
                .currency(currency)
                .totalAmount(totalAmount)
                .status(status)
                .orderItems(orderItems)
                .build();
    }

    private OrderItem getOrderItem(Long id, Long orderId, String productName, Integer quantity, BigDecimal unitPrice,
                                   BigDecimal lineTotal) {
        return OrderItem.builder()
                .id(id)
                .orderId(orderId)
                .productName(productName)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .build();
    }
}
