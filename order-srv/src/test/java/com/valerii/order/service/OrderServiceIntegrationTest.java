package com.valerii.order.service;

import com.valerii.order.config.TestcontainersConfiguration;
import com.valerii.order.exception.OrderNotFoundException;
import com.valerii.order.model.CreateOrderCommand;
import com.valerii.order.model.CreateOrderItemCommand;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.repository.order.OrderRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest()
@Transactional
@Import(TestcontainersConfiguration.class)
public class OrderServiceIntegrationTest {
    @Autowired
    OrderService orderService;

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    OrderRepository orderRepository;

    @Test
    void save_shouldPersistOrderWithItems() {
        Long customerId = 100L;
        String currency = "USD";
        OrderStatus expectedStatus = OrderStatus.CREATED;
        BigDecimal expectedTotalAmount = BigDecimal.valueOf(53.26);

        String expectedTeaProductName = "Tea";
        Integer expectedTeaQuantity = 2;
        BigDecimal expectedTeaUnitPrice = BigDecimal.valueOf(13.44);
        BigDecimal expectedTeaLineTotal = BigDecimal.valueOf(26.88);

        String expectedCoffeeProductName = "Coffee";
        Integer expectedCoffeeQuantity = 1;
        BigDecimal expectedCoffeeUnitPrice = BigDecimal.valueOf(26.38);
        BigDecimal expectedCoffeeLineTotal = BigDecimal.valueOf(26.38);

        CreateOrderCommand createOrderCommand = getCreateOrderCommand(customerId, currency,
                List.of(getCreateOrderItemCommand("Tea", 2, BigDecimal.valueOf(13.44)),
                        getCreateOrderItemCommand("Coffee", 1, BigDecimal.valueOf(26.38))));
        Order savedOrder = orderService.save(createOrderCommand);

        //assert order
        assertNotNull(savedOrder.getId());
        assertEquals(customerId, savedOrder.getCustomerId());
        assertEquals(currency, savedOrder.getCurrency());
        assertEquals(0, expectedTotalAmount.compareTo(savedOrder.getTotalAmount()));
        assertEquals(expectedStatus, savedOrder.getStatus());
        assertNotNull(savedOrder.getCreatedAt());
        assertNotNull(savedOrder.getUpdatedAt());

        // assert order items
        assertNotNull(savedOrder.getOrderItems());
        assertEquals(createOrderCommand.getOrderItems().size(), savedOrder.getOrderItems().size());

        OrderItem tea = savedOrder.getOrderItems().stream()
                .filter(oi -> oi.getProductName().equals("Tea"))
                .findFirst()
                .orElseThrow();

        assertEquals(expectedTeaProductName, tea.getProductName());
        assertEquals(0, expectedTeaUnitPrice.compareTo(tea.getUnitPrice()));
        assertEquals(expectedTeaQuantity, tea.getQuantity());
        assertEquals(0, expectedTeaLineTotal.compareTo(tea.getLineTotal()));
        assertEquals(savedOrder.getId(), tea.getOrderId());

        OrderItem coffee = savedOrder.getOrderItems().stream()
                .filter(oi -> oi.getProductName().equals("Coffee"))
                .findFirst()
                .orElseThrow();

        assertEquals(expectedCoffeeProductName, coffee.getProductName());
        assertEquals(0, expectedCoffeeUnitPrice.compareTo(coffee.getUnitPrice()));
        assertEquals(expectedCoffeeQuantity, coffee.getQuantity());
        assertEquals(0, expectedCoffeeLineTotal.compareTo(coffee.getLineTotal()));
        assertEquals(savedOrder.getId(), coffee.getOrderId());
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

    @Test
    void findById_shouldThrowOrderNotFoundException_whenNotFound() {
        Long customerId = 100L;
        String currency = "USD";
        CreateOrderCommand createOrderCommand = getCreateOrderCommand(customerId, currency,
                List.of(getCreateOrderItemCommand("Tea", 2, BigDecimal.valueOf(13.44)),
                        getCreateOrderItemCommand("Coffee", 1, BigDecimal.valueOf(26.38))));
        orderService.save(createOrderCommand);

        Long notExistedOrderId = -2L;

        assertThrows(OrderNotFoundException.class,() -> orderService.findById(notExistedOrderId));
    }

    @Test
    void save_shouldRejectOrderWithoutItems() {
        Long customerId = 100L;
        String currency = "USD";
        CreateOrderCommand createOrderCommand = getCreateOrderCommand(customerId, currency, emptyList());

        assertThrows(ConstraintViolationException.class,() -> orderService.save(createOrderCommand));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldRollbackOrderWithItems() {
        Long customerId = 100L;
        String currency = "USD";
        CreateOrderCommand createOrderCommand = getCreateOrderCommand(customerId, currency,
                List.of(getCreateOrderItemCommand("Tea", 2, BigDecimal.valueOf(13.44)),
                        getCreateOrderItemCommand("Coffee", 1, BigDecimal.valueOf(26.38))));
        String sql = "SELECT count(*) FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("customerId", customerId);
        Integer before = jdbcTemplate.queryForObject(sql, parameterSource, Integer.class);

        doThrow(new DataIntegrityViolationException("Test failure"))
                .when(orderRepository)
                .saveOrderItems(anyList(), anyLong());

        DataIntegrityViolationException exeption =
                assertThrows(DataIntegrityViolationException.class, () -> orderService.save(createOrderCommand));
        assertEquals("Test failure", exeption.getMessage());

        verify(orderRepository).saveOrder(any());
        verify(orderRepository).saveOrderItems(anyList(), anyLong());

        Integer after = jdbcTemplate.queryForObject(sql, parameterSource, Integer.class);
        assertEquals(before, after);
    }
}
