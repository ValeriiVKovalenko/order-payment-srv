package com.valerii.order;

import com.valerii.order.config.TestcontainersConfiguration;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.service.OrderService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
@Import(TestcontainersConfiguration.class)
public class OrderServiceValidationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    public void shouldThrowException_WhenCustomerIdIsNull() {

        OrderItem orderItemTea =
                getOrderItem("Tea", 2, BigDecimal.valueOf(100), BigDecimal.valueOf(200));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order invalidOrderDto = getOrder(null, "EUR", BigDecimal.valueOf(250), OrderStatus.COMPLETED, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });

        String sql = "SELECT count(*) FROM orders WHERE currency = :currency AND total_amount = :total_amount AND status = :status";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("currency", invalidOrderDto.getCurrency())
                .addValue("total_amount", invalidOrderDto.getTotalAmount())
                .addValue("status", invalidOrderDto.getStatus().name());
        Long orderCount = jdbcTemplate.queryForObject(sql, params, Long.class);
        assertEquals(0, orderCount);
    }

    @Test
    public void shouldThrowException_WhenCurrencyIsLowerCase() {

        OrderItem orderItemTea =
                getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order invalidOrderDto = getOrder(501L, "usd", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });

        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", invalidOrderDto.getCustomerId());
        Long orderCount = jdbcTemplate.queryForObject(sql, params, Long.class);
        assertEquals(0, orderCount);
    }

    @Test
    public void shouldThrowException_WhenCurrencyMoreThanThreeCharacters() {

        OrderItem orderItemTea =
                getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order invalidOrderDto = getOrder(502L, "USDX", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });

        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", invalidOrderDto.getCustomerId());
        Long orderCount = jdbcTemplate.queryForObject(sql, params, Long.class);
        assertEquals(0, orderCount);
    }

    @Test
    public void shouldThrowException_WhenOrderIsValid() {

        OrderItem orderItemTea =
                getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order validOrderDto =
                getOrder(1L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        Order savedOrder = orderService.save(validOrderDto);
        assertEquals(validOrderDto.getStatus(), savedOrder.getStatus());
        assertEquals(validOrderDto.getCustomerId(), savedOrder.getCustomerId());
        assertEquals(validOrderDto.getCurrency(), savedOrder.getCurrency());
        assertEquals(0, BigDecimal.valueOf(150).compareTo(savedOrder.getTotalAmount()));
    }

    @Test
    public void shouldThrowException_WhenOrderWithoutItemsIsValid() {
        Order validOrderDto =
                getOrder(1L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, emptyList());

        Order savedOrder = orderService.save(validOrderDto);
        assertEquals(validOrderDto.getStatus(), savedOrder.getStatus());
        assertEquals(validOrderDto.getCustomerId(), savedOrder.getCustomerId());
        assertEquals(validOrderDto.getCurrency(), savedOrder.getCurrency());
        assertEquals(0, BigDecimal.valueOf(150).compareTo(savedOrder.getTotalAmount()));
        assertTrue(savedOrder.getOrderItems().isEmpty());
    }

    @Test
    public void shouldThrowException_WhenProductNameMoreThanFiftyCharacters() {

        OrderItem orderItemTea =
                getOrderItem("Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_",
                        2, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order invalidOrderDto = getOrder(503L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });

        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", invalidOrderDto.getCustomerId());
        Long orderCount = jdbcTemplate.queryForObject(sql, params, Long.class);
        assertEquals(0, orderCount);
    }
    @Test
    public void shouldThrowException_WhenUnitPriceIsNegative() {

        OrderItem orderItemTea =
                getOrderItem("Tea",
                        2, BigDecimal.valueOf(-50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order invalidOrderDto = getOrder(504L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });

        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", invalidOrderDto.getCustomerId());
        Long orderCount = jdbcTemplate.queryForObject(sql, params, Long.class);
        assertEquals(0, orderCount);
    }

    @Test
    public void shouldThrowException_WhenQuantityIsZero() {

        OrderItem orderItemTea =
                getOrderItem("Tea",
                        0, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",0, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order invalidOrderDto = getOrder(505L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });

        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", invalidOrderDto.getCustomerId());
        Long orderCount = jdbcTemplate.queryForObject(sql, params, Long.class);
        assertEquals(0, orderCount);
    }

    @Test
    public void shouldThrowException_WhenLineTotalIsNegative() {

        OrderItem orderItemTea =
                getOrderItem("Tea",
                        2, BigDecimal.valueOf(50), BigDecimal.valueOf(-100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order invalidOrderDto = getOrder(506L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });

        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", invalidOrderDto.getCustomerId());
        Long orderCount = jdbcTemplate.queryForObject(sql, params, Long.class);
        assertEquals(0, orderCount);
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
}
