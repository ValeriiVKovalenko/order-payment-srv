package com.valerii.order.service;

import com.valerii.order.exception.OrderNotFoundException;
import com.valerii.order.mapper.OrderMapper;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.OrderSummaryDto;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.entity.OrderItemEntity;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.model.request.PageRequestParamDto;
import com.valerii.order.model.request.filter.OrderFilterRequestDto;
import com.valerii.order.model.response.page.PageResponseDto;
import com.valerii.order.repository.order.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;


    @Test
    void findById_shouldReturnOrder_whenExists() {
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
    void findById_shouldReturnNotFound_whenNotFound() {
        //given
        Long orderId = 99L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(orderId))
                .isInstanceOf(OrderNotFoundException.class);

        verifyNoInteractions(orderMapper);
    }

    @Test
    void findALL_shouldReturnOrdersFirstPage() {
        //given
        int page = 0;
        int size = 2;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);
        Long totalElements = 3L;
        OrderFilterRequestDto orderFilter = getOrderFilter(null, null);
        when(orderRepository.findOrderTotalElements(orderFilter)).thenReturn(totalElements);

        List<OrderEntity> orderEntities = getOrderEntityList();
        when(orderRepository.findAll(orderFilter, pagingParams.getSize(), pagingParams.getOffset()))
                .thenReturn(orderEntities);

        List<OrderSummaryDto> orders = getOrderList();
        when(orderMapper.toSummaryDtoList(orderEntities)).thenReturn(orders);

        Long totalPages = 2L;
        boolean hasNext = true;
        boolean hasPrevious = false;

        //when
        PageResponseDto<OrderSummaryDto> result = orderService.findAll(orderFilter, page, size);

        //then
        assertNotNull(result);
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(totalElements, result.getTotalElements());
        assertEquals(totalPages, result.getTotalPages());
        assertEquals(hasNext, result.getHasNext());
        assertEquals(hasPrevious, result.getHasPrevious());
        assertNotNull(result.getContent());
        assertFalse(result.getContent().isEmpty());
        assertThat(result.getContent()).hasSize(2);

        verify(orderRepository).findOrderTotalElements(orderFilter);
        verify(orderRepository).findAll(orderFilter, pagingParams.getSize(), pagingParams.getOffset());
        verify(orderMapper).toSummaryDtoList(orderEntities);
    }

    @Test
    void findALL_shouldReturnOrdersByFilter() {
        //given
        int page = 0;
        int size = 2;
        String status = "CREATED";
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);
        Long totalElements = 3L;
        OrderFilterRequestDto orderFilter = getOrderFilter(null, status);
        when(orderRepository.findOrderTotalElements(orderFilter)).thenReturn(totalElements);

        List<OrderEntity> orderEntities = getOrderEntityList();
        when(orderRepository.findAll(orderFilter, pagingParams.getSize(), pagingParams.getOffset()))
                .thenReturn(orderEntities);

        List<OrderSummaryDto> orders = getOrderList();
        when(orderMapper.toSummaryDtoList(orderEntities)).thenReturn(orders);

        Long totalPages = 2L;
        boolean hasNext = true;
        boolean hasPrevious = false;

        //when
        PageResponseDto<OrderSummaryDto> result = orderService.findAll(orderFilter, page, size);

        //then
        assertNotNull(result);
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(totalElements, result.getTotalElements());
        assertEquals(totalPages, result.getTotalPages());
        assertEquals(hasNext, result.getHasNext());
        assertEquals(hasPrevious, result.getHasPrevious());
        assertNotNull(result.getContent());
        assertFalse(result.getContent().isEmpty());
        assertThat(result.getContent()).hasSize(2);

        verify(orderRepository).findOrderTotalElements(orderFilter);
        verify(orderRepository).findAll(orderFilter, pagingParams.getSize(), pagingParams.getOffset());
        verify(orderMapper).toSummaryDtoList(orderEntities);
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

    private List<OrderSummaryDto> getOrderList () {
        List<OrderSummaryDto> orders = new ArrayList<>();
        String currency = "USD";

        OrderSummaryDto order1 = getOrderSummary(100L, 101L, currency, new BigDecimal("53.26"), OrderStatus.CREATED,
                OffsetDateTime.of(2026, 10, 26, 23, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 23, 28, 0, 0, ZoneOffset.UTC));
        orders.add(order1);
        OrderSummaryDto order2 = getOrderSummary(101L, 101L, currency, new BigDecimal("13.44"), OrderStatus.CREATED,
                OffsetDateTime.of(2026, 10, 26, 22, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 22, 28, 0, 0, ZoneOffset.UTC));
        orders.add(order2);

        return orders;
    }


    private List<OrderEntity> getOrderEntityList() {
        List<OrderEntity> orderEntities = new ArrayList<>(3);
        OrderEntity orderEntity1 = getOrderEntity(100L, 101L, new BigDecimal("53.26"), "USD", "CREATED",
                OffsetDateTime.of(2026, 10, 26, 23, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 23, 28, 0, 0, ZoneOffset.UTC),
                List.of(getItemEntity(1L, 100L, "Tea", 2, new BigDecimal("13.44"),
                                new BigDecimal("26.88")),
                        getItemEntity(2L, 100L, "Coffee", 1, new BigDecimal("26.38"),
                                new BigDecimal("26.38"))));
        orderEntities.add(orderEntity1);

        OrderEntity orderEntity2 = getOrderEntity(101L, 101L, new BigDecimal("62.9"), "USD", "CREATED",
                OffsetDateTime.of(2026, 10, 26, 22, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 22, 28, 0, 0, ZoneOffset.UTC),
                List.of(getItemEntity(3L, 101L, "Pizza", 3, new BigDecimal("15.64"),
                                new BigDecimal("46.92")),
                        getItemEntity(4L, 101L, "Kebab", 2, new BigDecimal("7.99"),
                                new BigDecimal("15.98"))));
        orderEntities.add(orderEntity2);

        return orderEntities;
    }

    private OrderSummaryDto getOrderSummary(Long id, Long customerId, String currency, BigDecimal totalAmount, OrderStatus status,
                                            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        return OrderSummaryDto.builder()
                .id(id)
                .customerId(customerId)
                .currency(currency)
                .totalAmount(totalAmount)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    private OrderFilterRequestDto getOrderFilter(Long customerId, String status) {
        return OrderFilterRequestDto.builder()
                .customerId(customerId)
                .status(status)
                .build();
    }
}
