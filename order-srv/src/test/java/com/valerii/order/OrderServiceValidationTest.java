package com.valerii.order;

import com.valerii.order.config.TestcontainersConfiguration;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.service.OrderService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
@Import(TestcontainersConfiguration.class)
public class OrderServiceValidationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @ParameterizedTest(name = "customerId={0} should be rejected")
    @NullSource
    @ValueSource(longs = {-1L, 0L})
    public void shouldThrowException_WhenCustomerIsInvalid(Long customerId) {
        Order invalidOrder = getOrder(
                customerId,
                "EUR",
                BigDecimal.valueOf(150),
                OrderStatus.CREATED,
                List.of(getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                        getOrderItem("Coffee", 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50))
                )
        );
        assertThrows(ConstraintViolationException.class, () -> {
            Order save = orderService.save(invalidOrder);
        });

        if (customerId != null) {
            String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
            MapSqlParameterSource params = new MapSqlParameterSource("customerId", customerId);
            Long orderCount = jdbcTemplate.queryForObject(sql, params, Long.class);
            assertEquals(0, orderCount);
        } else {
            String sql = "SELECT count(*) FROM orders WHERE customer_id IS NULL";
            Long orderCount = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
            assertEquals(0, orderCount);
        }

    }

    @Test
    public void shouldThrowException_WhenStatusIsNull() {

        Order invalidOrderDto = getOrder(
                401L,
                "EUR",
                BigDecimal.valueOf(150),
                null,
                List.of(getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                        getOrderItem("Coffee", 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50))
                )
        );
        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });

        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", invalidOrderDto.getCustomerId());
        Long orderCount = jdbcTemplate.queryForObject(sql, params, Long.class);
        assertEquals(0, orderCount);
    }

    @ParameterizedTest(name = "currency={0} should be rejected")
    @NullSource
    @ValueSource(strings = {"", "   ", "usd", "USDX", "U$D"})
    public void shouldThrowException_WhenInvalidCurrency(String currency) {
        Order invalidOrder = getOrder(
                507L,
                currency,
                BigDecimal.valueOf(150),
                OrderStatus.CREATED,
                List.of(getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                        getOrderItem("Coffee", 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50))
                )
        );

        assertThrows(
                ConstraintViolationException.class,
                () -> orderService.save(invalidOrder)
        );

        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", invalidOrder.getCustomerId());
        Long orderCount = jdbcTemplate.queryForObject(sql, params, Long.class);
        assertEquals(0, orderCount);
    }

    @ParameterizedTest(name = "totalAmount={0} should be rejected")
    @NullSource
    @MethodSource("invalidUnitPriceAndLineTotal")
    public void shouldThrowException_WhenInvalidTotalAmount(BigDecimal totalAmount) {
        Order invalidOrder = getOrder(
                507L,
                "USD",
                totalAmount,
                OrderStatus.CREATED,
                List.of(getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                        getOrderItem("Coffee", 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50))
                )
        );

        assertThrows(
                ConstraintViolationException.class,
                () -> orderService.save(invalidOrder)
        );

        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", invalidOrder.getCustomerId());
        Long orderCount = jdbcTemplate.queryForObject(sql, params, Long.class);
        assertEquals(0, orderCount);
    }

   @Test
    public void shouldThrowException_WhenOrderItemsIsNull() {
        Order invalidOrder = getOrder(507L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED,
                null);

        assertThrows(ConstraintViolationException.class, () -> orderService.save(invalidOrder));

        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", invalidOrder.getCustomerId());
        Long orderCount = jdbcTemplate.queryForObject(sql, params, Long.class);
        assertEquals(0, orderCount);
    }

    @Test
    public void shouldSaveOrderWhenValid() {

        Order validOrderDto = getOrder(
                201L,
                "EUR",
                BigDecimal.valueOf(150),
                OrderStatus.CREATED,
                List.of(getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                        getOrderItem("Coffee", 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50))
                )
        );

        Order savedOrder = orderService.save(validOrderDto);
        assertEquals(validOrderDto.getStatus(), savedOrder.getStatus());
        assertEquals(validOrderDto.getCustomerId(), savedOrder.getCustomerId());
        assertEquals(validOrderDto.getCurrency(), savedOrder.getCurrency());
        assertEquals(0, BigDecimal.valueOf(150).compareTo(savedOrder.getTotalAmount()));
    }

    @Test
    public void shouldSaveOrderWhenItemsAreEmpty() {
        Order validOrderDto =
                getOrder(1L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, emptyList());

        Order savedOrder = orderService.save(validOrderDto);
        assertEquals(validOrderDto.getStatus(), savedOrder.getStatus());
        assertEquals(validOrderDto.getCustomerId(), savedOrder.getCustomerId());
        assertEquals(validOrderDto.getCurrency(), savedOrder.getCurrency());
        assertEquals(0, BigDecimal.valueOf(150).compareTo(savedOrder.getTotalAmount()));
        assertTrue(savedOrder.getOrderItems().isEmpty());
    }

    @ParameterizedTest(name = "productName={0} should be rejected")
    @NullSource
    @ValueSource(strings = {"", "   ", "Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_"})
    public void shouldThrowException_WhenProductNameIsInvalid(String productName) {

        OrderItem orderItemTea =
                getOrderItem(productName, 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
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

    @ParameterizedTest(name = "quantity={0} should be rejected")
    @NullSource
    @ValueSource(ints = {-1, 0})
    public void shouldThrowException_WhenQuantityIsInvalid(Integer quantity) {

        OrderItem orderItemTea =
                getOrderItem("Tea",
                        quantity, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

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

    @ParameterizedTest(name = "unitPrice={0} should be rejected")
    @NullSource
    @MethodSource("invalidUnitPriceAndLineTotal")
    public void shouldThrowException_WhenUnitPriceIsInvalid(BigDecimal unitPrice) {

        OrderItem orderItemTea =
                getOrderItem("Tea",
                        2, unitPrice, BigDecimal.valueOf(100));
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

    static Stream<BigDecimal> invalidUnitPriceAndLineTotal() {
        return Stream.of(BigDecimal.valueOf(-50L));
    }

    @ParameterizedTest(name = "lineTotal={0} should be rejected")
    @NullSource
    @MethodSource("invalidUnitPriceAndLineTotal")
    public void shouldThrowException_WhenLineTotalIsInvalid(BigDecimal lineTotal) {

        OrderItem orderItemTea =
                getOrderItem("Tea",
                        2, BigDecimal.valueOf(50), lineTotal);
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
