package com.valerii.order.factory;

import com.valerii.order.model.*;
import com.valerii.order.model.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.valerii.order.model.OrderFactory.createOrder;
import static org.junit.jupiter.api.Assertions.*;

public class OrderFactoryTest {

    @Test
    void should_correctlyCreateOrderFromOrderCommand() {
        Long customerId = 101L;
        String currency = "USD";
        OrderStatus orderStatus = OrderStatus.CREATED;
        BigDecimal totalAmount = BigDecimal.valueOf(53.26);
        CreateOrderCommand createInvalidOrderCommand = getCreateOrderCommand(customerId, currency,
                List.of(getCreateOrderItemCommand("Tea", 2, BigDecimal.valueOf(13.44)),
                        getCreateOrderItemCommand("Coffee", 1, BigDecimal.valueOf(26.38))));
        Order result = createOrder(createInvalidOrderCommand);

        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(currency, result.getCurrency());
        assertEquals(orderStatus, result.getStatus());
        assertEquals(totalAmount, result.getTotalAmount());

        assertNotNull(result.getOrderItems());
        assertFalse(result.getOrderItems().isEmpty());

        OrderItem tea = result.getOrderItems().stream()
                .filter(oi -> oi.getProductName().equals("Tea"))
                .findFirst()
                .orElseThrow();

        assertEquals("Tea", tea.getProductName());
        assertEquals(0, BigDecimal.valueOf(13.44).compareTo(tea.getUnitPrice()));
        assertEquals(2, tea.getQuantity());
        assertEquals(0, BigDecimal.valueOf(26.88).compareTo(tea.getLineTotal()));

        OrderItem coffee = result.getOrderItems().stream()
                .filter(oi -> oi.getProductName().equals("Coffee"))
                .findFirst()
                .orElseThrow();

        assertEquals("Coffee", coffee.getProductName());
        assertEquals(0, BigDecimal.valueOf(26.38).compareTo(coffee.getUnitPrice()));
        assertEquals(1, coffee.getQuantity());
        assertEquals(0, BigDecimal.valueOf(26.38).compareTo(coffee.getLineTotal()));
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
