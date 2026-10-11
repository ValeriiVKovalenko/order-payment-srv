package com.valerii.order;

import com.valerii.order.config.TestcontainersConfiguration;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.repository.order.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.valerii.order.service.OrderService;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
@Transactional
@Import(TestcontainersConfiguration.class)
public class OrderIntegrationTest {
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void save_shouldPersistOrderWithItems() {
        OrderItem orderItemTea =
                getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order order = getOrder(100L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        //assert order
        Order savedOrder = orderService.save(order);
        assertNotNull(savedOrder.getId());
        assertEquals(100L, savedOrder.getCustomerId());
        assertEquals("USD", savedOrder.getCurrency());
        assertEquals(0, BigDecimal.valueOf(150).compareTo(savedOrder.getTotalAmount()));
        assertEquals(OrderStatus.CREATED, savedOrder.getStatus());
        assertNotNull(savedOrder.getCreatedAt());
        assertNotNull(savedOrder.getUpdatedAt());

        // assert order items
        assertNotNull(savedOrder.getOrderItems());
        assertEquals(orderItems.size(), savedOrder.getOrderItems().size());

        OrderItem tea = savedOrder.getOrderItems().stream()
                .filter(oi -> oi.getProductName().equals("Tea"))
                .findFirst()
                .orElseThrow();

        assertEquals(orderItemTea.getProductName(), tea.getProductName());
        assertEquals(0, orderItemTea.getUnitPrice().compareTo(tea.getUnitPrice()));
        assertEquals(orderItemTea.getQuantity(), tea.getQuantity());
        assertEquals(0, orderItemTea.getLineTotal().compareTo(tea.getLineTotal()));
        assertEquals(savedOrder.getId(), tea.getOrderId());

        OrderItem coffee = savedOrder.getOrderItems().stream()
                .filter(oi -> oi.getProductName().equals("Coffee"))
                .findFirst()
                .orElseThrow();

        assertEquals(orderItemCoffee.getProductName(), coffee.getProductName());
        assertEquals(0, orderItemCoffee.getUnitPrice().compareTo(coffee.getUnitPrice()));
        assertEquals(orderItemCoffee.getQuantity(), coffee.getQuantity());
        assertEquals(0,  orderItemCoffee.getLineTotal().compareTo(coffee.getLineTotal()));
        assertEquals(savedOrder.getId(), coffee.getOrderId());

    }

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
    void find_shouldReturnOptionalEmpty() {
        OrderItem orderItemTea =
                getOrderItem("Tee", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order order = getOrder(100L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);
        orderService.save(order);
        Long notExistedOrderId = -2L;

        Optional<OrderEntity> orderEntity = orderRepository.findById(notExistedOrderId);

        assertFalse(orderEntity.isPresent());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldRollbackBySavingOrderItems() {
        OrderItem productWithMoreThan50Symbols =
                getOrderItem("ProductWithMoreThan50Symbols_ProductWithMoreThan50Symbols",
                        1, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(productWithMoreThan50Symbols);
        Order order = getOrder(201L, "USD", BigDecimal.valueOf(100), OrderStatus.CREATED, orderItems);
        assertThrows(DataIntegrityViolationException.class, () -> {orderService.save(order);});

        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", 201L);
        Long orderCount = jdbcTemplate.queryForObject(sql, params, Long.class);
        assertEquals(0, orderCount);
    }
}
