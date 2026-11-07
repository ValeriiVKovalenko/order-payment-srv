package com.valerii.order.repository;

import com.valerii.order.config.TestcontainersConfiguration;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.entity.OrderItemEntity;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.model.request.PageRequestParamDto;
import com.valerii.order.model.request.filter.OrderFilterRequestDto;
import com.valerii.order.repository.order.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
@Transactional
@Import(TestcontainersConfiguration.class)
public class OrderRepositoryIntegrationTest {
    @Autowired
    OrderRepository orderRepository;

    private Order getOrder(Long customerId, String currency, BigDecimal totalAmount, OrderStatus status,
                           List<OrderItem> orderItems) {
        return Order.builder()
                .customerId(customerId)
                .currency(currency)
                .totalAmount(totalAmount)
                .status(status)
                .orderItems(orderItems)
                .build();
    }

    private OrderItem getOrderItem(String productName, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        return OrderItem.builder()
                .productName(productName)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .build();
    }

    @Test
    void find_shouldReturnOptionalEmptyWhenOrderWithDifferentIdExists() {
        OrderItem orderItemTea =
                getOrderItem("Tee", 2, new BigDecimal("50"), new BigDecimal("100"));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, new BigDecimal("50"), new BigDecimal("50"));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order order = getOrder(100L, "USD", new BigDecimal("150"), OrderStatus.CREATED, orderItems);
        Long notExistedOrderId = -2L;
        OrderEntity orderEntity = orderRepository.saveOrder(order);
        orderRepository.saveOrderItems(orderItems, orderEntity.getId());

        Optional<OrderEntity> result = orderRepository.findById(notExistedOrderId);

        assertFalse(result.isPresent());
    }

    @Test
    void find_shouldReturnOrderWithItems() {
        OrderItem orderItemTea =
                getOrderItem("Tea", 2, new BigDecimal("50"), new BigDecimal("100"));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, new BigDecimal("50"), new BigDecimal("50"));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        //saved order and orderItems
        Order order = getOrder(100L, "USD", new BigDecimal("150"), OrderStatus.CREATED, orderItems);
        OrderEntity orderEntityWithId = orderRepository.saveOrder(order);
        orderRepository.saveOrderItems(order.getOrderItems(), orderEntityWithId.getId());

        //find order
        OrderEntity orderEntity = orderRepository.findById(orderEntityWithId.getId())
                .orElseThrow();

        //assert Order
        assertNotNull(orderEntity);
        assertEquals(orderEntityWithId.getId(), orderEntity.getId());
        assertEquals(order.getCustomerId(), orderEntity.getCustomerId());
        assertEquals(0, order.getTotalAmount().compareTo(orderEntity.getTotalAmount()));
        assertEquals(order.getStatus().name(), orderEntity.getStatus());
        assertEquals(order.getCurrency(), orderEntity.getCurrency());

        //assert orderItems
        assertNotNull(orderEntity.getOrderItemsEntity());
        assertEquals(order.getOrderItems().size(), orderEntity.getOrderItemsEntity().size());

        //Tea
        OrderItemEntity teeOrderItemEntity = orderEntity.getOrderItemsEntity().stream()
                .filter(productName -> "Tea".equals(productName.getProductName()))
                .findFirst()
                .orElseThrow();

        assertNotNull(teeOrderItemEntity.getId());
        assertEquals(orderItemTea.getProductName(), teeOrderItemEntity.getProductName());
        assertEquals(0, orderItemTea.getUnitPrice().compareTo(teeOrderItemEntity.getUnitPrice()));
        assertEquals(orderItemTea.getQuantity(), teeOrderItemEntity.getQuantity());
        assertEquals(0, orderItemTea.getLineTotal().compareTo(teeOrderItemEntity.getLineTotal()));
        assertEquals(orderEntityWithId.getId(), teeOrderItemEntity.getOrderId());

        //Coffee
        OrderItemEntity coffeeOrderItemEntity = orderEntity.getOrderItemsEntity().stream()
                .filter(productName -> "Coffee".equals(productName.getProductName()))
                .findFirst()
                .orElseThrow();

        assertNotNull(coffeeOrderItemEntity.getId());
        assertEquals(orderItemCoffee.getProductName(), coffeeOrderItemEntity.getProductName());
        assertEquals(0, orderItemCoffee.getUnitPrice().compareTo(coffeeOrderItemEntity.getUnitPrice()));
        assertEquals(orderItemCoffee.getQuantity(), coffeeOrderItemEntity.getQuantity());
        assertEquals(0, orderItemCoffee.getLineTotal().compareTo(coffeeOrderItemEntity.getLineTotal()));
        assertEquals(orderEntityWithId.getId(), coffeeOrderItemEntity.getOrderId());
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findALL_shouldReturnOrderWithFirstPage() {
        Integer page = 0;
        Integer size = 10;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);
        OrderFilterRequestDto orderFilter = getOrderFilter(null, null);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).hasSize(10)
                .extracting(OrderEntity::getId)
                .containsExactly(50L, 49L, 48L, 47L, 46L, 45L, 44L, 43L, 42L, 41L);
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findALL_shouldReturnOrderWithSecondPage() {
        Integer page = 1;
        Integer size = 10;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);
        OrderFilterRequestDto orderFilter = getOrderFilter(null, null);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).hasSize(10)
                .extracting(OrderEntity::getId)
                .containsExactly(40L, 39L, 38L, 37L, 36L, 35L, 34L, 33L, 32L, 31L);
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findALL_shouldReturnOrderOrderByCreatedAt() {
        Integer page = 0;
        Integer size = 50;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);
        OrderFilterRequestDto orderFilter = getOrderFilter(null, null);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).hasSize(50);

        assertThat(orders).extracting(OrderEntity::getCreatedAt)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void count_shouldReturnTotalNumberOfOrders() {
        OrderFilterRequestDto orderFilter = getOrderFilter(null, null);
        Long total = orderRepository.findOrderTotalElements(orderFilter);

        assertThat(total).isEqualTo(50);
    }

    private OrderFilterRequestDto getOrderFilter(Long customerId, String status) {
        return OrderFilterRequestDto.builder()
                .customerId(customerId)
                .status(status)
                .build();
    }

    @Test
    void findALL_shouldReturnEmptyList() {
        Integer page = 0;
        Integer size = 50;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);
        OrderFilterRequestDto orderFilter = getOrderFilter(null, null);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).isEmpty();
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findAll_shouldReturnEmptyListWhenPageIsOutOfRange() {
        int page = 100;
        int size = 10;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);
        OrderFilterRequestDto orderFilter = getOrderFilter(null, null);

        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).isEmpty();
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findAll_shouldReturnLastNonEmptyPage() {
        int page = 4;
        int size = 10;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);

        OrderFilterRequestDto orderFilter = getOrderFilter(null, null);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).hasSize(10);
        assertThat(orders)
                .extracting(OrderEntity::getId)
                .containsExactly(10L, 9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L);
    }
}

