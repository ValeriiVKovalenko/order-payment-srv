package com.valerii.order.repository;

import com.valerii.order.config.TestcontainersConfiguration;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.entity.OrderItemEntity;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.repository.order.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                getOrderItem("Tee", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order order = getOrder(100L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);
        Long notExistedOrderId = -2L;
        OrderEntity orderEntity = orderRepository.saveOrder(order);
        orderRepository.saveOrderItems(orderItems, orderEntity.getId());

        Optional<OrderEntity> result = orderRepository.findById(notExistedOrderId);

        assertFalse(result.isPresent());
    }

    @Test
    void find_shouldReturnOrderWithItems() {
        OrderItem orderItemTea =
                getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        //saved order and orderItems
        Order order = getOrder(100L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);
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
}

