package com.valerii.order.validation;

import com.valerii.order.config.TestcontainersConfiguration;
import com.valerii.order.model.CreateOrderCommand;
import com.valerii.order.model.CreateOrderItemCommand;
import com.valerii.order.model.Order;
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
    void shouldThrowException_WhenCustomerIsInvalid(Long customerId) {
        CreateOrderCommand createInvalidOrderCommand = getCreateOrderCommand(customerId, "USD",
                List.of(getCreateOrderItemCommand("Tea", 2, BigDecimal.valueOf(13.44)),
                        getCreateOrderItemCommand("Coffee", 1, BigDecimal.valueOf(26.38))));

        assertOrderCountUnchanged(customerId, createInvalidOrderCommand);
    }

    private Long getCountOrdersByCustomerId(Long customerId) {
        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", customerId);
        return jdbcTemplate.queryForObject(sql, params, Long.class);
    }

    private void assertOrderCountUnchanged(Long customerId, CreateOrderCommand order) {
        Long before = getCountOrdersByCustomerId(customerId);
        assertThrows(ConstraintViolationException.class, () -> orderService.save(order));
        Long after = getCountOrdersByCustomerId(customerId);

        assertEquals(before, after);
    }

    @ParameterizedTest(name = "currency={0} should be rejected")
    @NullSource
    @ValueSource(strings = {"", "   ", "usd", "USDX", "U$D"})
    void shouldThrowException_WhenInvalidCurrency(String currency) {
        Long customerId = 201L;
        CreateOrderCommand createInvalidOrderCommand = getCreateOrderCommand(customerId, currency,
                List.of(getCreateOrderItemCommand("Tea", 2, BigDecimal.valueOf(13.44)),
                        getCreateOrderItemCommand("Coffee", 1, BigDecimal.valueOf(26.38))));
        assertOrderCountUnchanged(customerId, createInvalidOrderCommand);
    }

    static Stream<BigDecimal> negativePrice() {
        return Stream.of(BigDecimal.valueOf(-50L));
    }

    @Test
    void shouldThrowException_WhenOrderItemsIsNull() {
        Long customerId = 201L;
        CreateOrderCommand createInvalidOrderCommand = getCreateOrderCommand(customerId, "USD", null);

        Long before = getCountOrdersByCustomerId(customerId);
        assertThrows(NullPointerException.class, () -> orderService.save(createInvalidOrderCommand));
        Long after = getCountOrdersByCustomerId(customerId);

        assertEquals(before, after);
    }

    @Test
    void shouldSaveOrderWhenValid() {
        Long customerId = 201L;
        String currency = "USD";
        BigDecimal totalAmount = BigDecimal.valueOf(53.26);
        CreateOrderCommand createOrderCommand = getCreateOrderCommand(customerId, currency,
                List.of(getCreateOrderItemCommand("Tea", 2, BigDecimal.valueOf(13.44)),
                        getCreateOrderItemCommand("Coffee", 1, BigDecimal.valueOf(26.38))));

        Order savedOrder = orderService.save(createOrderCommand);
        assertEquals(OrderStatus.CREATED, savedOrder.getStatus());
        assertEquals(customerId, savedOrder.getCustomerId());
        assertEquals(currency, savedOrder.getCurrency());
        assertEquals(0, totalAmount.compareTo(savedOrder.getTotalAmount()));
    }

    @Test
    void shouldThrowException_whenItemsAreEmpty() {
        Long customerId = 201L;
        String currency = "USD";
        CreateOrderCommand createInvalidOrderCommand = getCreateOrderCommand(customerId, currency, emptyList());

        assertOrderCountUnchanged(customerId, createInvalidOrderCommand);
    }

    @ParameterizedTest(name = "productName={0} should be rejected")
    @NullSource
    @ValueSource(strings = {"", "   ", "Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_Tea_"})
    void shouldThrowException_WhenProductNameIsInvalid(String productName) {
        Long customerId = 201L;
        String currency = "USD";
        CreateOrderCommand createInvalidOrderCommand = getCreateOrderCommand(customerId, currency,
                List.of(getCreateOrderItemCommand("Tea", 2, BigDecimal.valueOf(13.44)),
                        getCreateOrderItemCommand(productName, 1, BigDecimal.valueOf(26.38))));
        assertOrderCountUnchanged(customerId, createInvalidOrderCommand);
    }

    @ParameterizedTest(name = "quantity={0} should be rejected")
    @NullSource
    @ValueSource(ints = {-1, 0})
    void shouldThrowException_WhenQuantityIsInvalid(Integer quantity) {
        Long customerId = 201L;
        String currency = "USD";
        CreateOrderCommand createInvalidOrderCommand = getCreateOrderCommand(customerId, currency,
                List.of(getCreateOrderItemCommand("Tea", 2, BigDecimal.valueOf(13.44)),
                        getCreateOrderItemCommand("Coffee", quantity, BigDecimal.valueOf(26.38))));

        if (quantity == null) {
            Long before = getCountOrdersByCustomerId(customerId);
            assertThrows(NullPointerException.class, () -> orderService.save(createInvalidOrderCommand));
            Long after = getCountOrdersByCustomerId(customerId);

            assertEquals(before, after);
        } else {
            assertOrderCountUnchanged(customerId, createInvalidOrderCommand);
        }
    }

    @ParameterizedTest(name = "unitPrice={0} should be rejected")
    @NullSource
    @MethodSource("negativePrice")
    void shouldThrowException_WhenUnitPriceIsInvalid(BigDecimal unitPrice) {
        Long customerId = 201L;
        String currency = "USD";
        CreateOrderCommand createInvalidOrderCommand = getCreateOrderCommand(customerId, currency,
                List.of(getCreateOrderItemCommand("Tea", 2, BigDecimal.valueOf(13.44)),
                        getCreateOrderItemCommand("Coffee", 1, unitPrice)));

        if (unitPrice == null) {
            Long before = getCountOrdersByCustomerId(customerId);
            assertThrows(NullPointerException.class, () -> orderService.save(createInvalidOrderCommand));
            Long after = getCountOrdersByCustomerId(customerId);

            assertEquals(before, after);
        } else {
            assertOrderCountUnchanged(customerId, createInvalidOrderCommand);
        }
    }

    private CreateOrderItemCommand getCreateOrderItemCommand(String productName, Integer quantity,
                                                             BigDecimal unitPrice) {
        return CreateOrderItemCommand.builder()
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();
    }

    private CreateOrderCommand getCreateOrderCommand(Long customerId, String currency,
                                                     List<CreateOrderItemCommand> orderItemRequestDtoList) {
        return CreateOrderCommand.builder()
                .customerId(customerId)
                .currency(currency)
                .orderItems(orderItemRequestDtoList)
                .build();
    }
}
