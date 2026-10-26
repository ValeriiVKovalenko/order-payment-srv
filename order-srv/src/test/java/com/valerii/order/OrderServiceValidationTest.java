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

        if (customerId != null) {
            assertOrderCountUnchanged(customerId, invalidOrder);
        }
    }

    private Long getCountOrdersByCustomerId(Long customerId) {
        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", customerId);
        return jdbcTemplate.queryForObject(sql, params, Long.class);
    }

    private void assertOrderCountUnchanged(Long customerId, Order order) {
        Long orderCountBeforeSave = getCountOrdersByCustomerId(customerId);
        assertThrows(ConstraintViolationException.class, () -> {orderService.save(order);});
        Long orderCountAfterSave = getCountOrdersByCustomerId(customerId);

        assertEquals(orderCountBeforeSave, orderCountAfterSave);
    }

    @Test
    public void shouldThrowException_WhenStatusIsNull() {
        Long customerId = 201L;
        Order invalidOrder = getOrder(
                customerId,
                "EUR",
                BigDecimal.valueOf(150),
                null,
                List.of(getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                        getOrderItem("Coffee", 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50))
                )
        );
        assertOrderCountUnchanged(customerId, invalidOrder);
    }

    @ParameterizedTest(name = "currency={0} should be rejected")
    @NullSource
    @ValueSource(strings = {"", "   ", "usd", "USDX", "U$D"})
    public void shouldThrowException_WhenInvalidCurrency(String currency) {
        Long customerId = 201L;
        Order invalidOrder = getOrder(
                customerId,
                currency,
                BigDecimal.valueOf(150),
                OrderStatus.CREATED,
                List.of(getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                        getOrderItem("Coffee", 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50))
                )
        );
        assertOrderCountUnchanged(customerId, invalidOrder);
    }

    @ParameterizedTest(name = "totalAmount={0} should be rejected")
    @NullSource
    @MethodSource("negativePrice")
    public void shouldThrowException_WhenInvalidTotalAmount(BigDecimal totalAmount) {
        Long customerId = 201L;
        Order invalidOrder = getOrder(
                customerId,
                "USD",
                totalAmount,
                OrderStatus.CREATED,
                List.of(getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                        getOrderItem("Coffee", 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50))
                )
        );

        assertOrderCountUnchanged(customerId, invalidOrder);
    }

    static Stream<BigDecimal> negativePrice() {
        return Stream.of(BigDecimal.valueOf(-50L));
    }

   @Test
    public void shouldThrowException_WhenOrderItemsIsNull() {
       Long customerId = 201L;
        Order invalidOrder = getOrder(customerId, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED,
                null);

       assertOrderCountUnchanged(customerId, invalidOrder);
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
        Long customerId = 201L;
        Order invalidOrder = getOrder(
                201L,
                "EUR",
                BigDecimal.valueOf(150),
                OrderStatus.CREATED,
                List.of(getOrderItem(productName, 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                        getOrderItem(productName, 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50))
                )
        );
        assertOrderCountUnchanged(customerId, invalidOrder);
    }

    @ParameterizedTest(name = "quantity={0} should be rejected")
    @NullSource
    @ValueSource(ints = {-1, 0})
    public void shouldThrowException_WhenQuantityIsInvalid(Integer quantity) {
        Long customerId = 201L;
        Order invalidOrder = getOrder(
                201L,
                "EUR",
                BigDecimal.valueOf(150),
                OrderStatus.CREATED,
                List.of(getOrderItem("Tea", quantity, BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                        getOrderItem("Coffee", 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50))
                )
        );

        assertOrderCountUnchanged(customerId, invalidOrder);
    }

    @ParameterizedTest(name = "unitPrice={0} should be rejected")
    @NullSource
    @MethodSource("negativePrice")
    public void shouldThrowException_WhenUnitPriceIsInvalid(BigDecimal unitPrice) {
        Long customerId = 201L;
        Order invalidOrder = getOrder(
                201L,
                "EUR",
                BigDecimal.valueOf(150),
                OrderStatus.CREATED,
                List.of(getOrderItem("Tea", 2, unitPrice, BigDecimal.valueOf(100)),
                        getOrderItem("Coffee", 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50))
                )
        );

        assertOrderCountUnchanged(customerId, invalidOrder);
    }

    @ParameterizedTest(name = "lineTotal={0} should be rejected")
    @NullSource
    @MethodSource("negativePrice")
    public void shouldThrowException_WhenLineTotalIsInvalid(BigDecimal lineTotal) {
        Long customerId = 201L;
        Order invalidOrder = getOrder(
                201L,
                "EUR",
                BigDecimal.valueOf(150),
                OrderStatus.CREATED,
                List.of(getOrderItem("Tea", 2, BigDecimal.valueOf(50), lineTotal),
                        getOrderItem("Coffee", 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50))
                )
        );

        assertOrderCountUnchanged(customerId, invalidOrder);
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
