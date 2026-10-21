package com.valerii.order;

import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.service.OrderService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest()
public class OrderServiceValidationTest {

    @Autowired
    private OrderService orderService;

    @Test
    public void shouldThrowException_WhenCustomerIdIsNull() {

        OrderItem orderItemTea =
                getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order invalidOrderDto = getOrder(null, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });
    }

    @Test
    public void shouldThrowException_WhenCustomerIdIsNullAndCurrencyMoreThanThreeCharacters() {

        OrderItem orderItemTea =
                getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order invalidOrderDto = getOrder(null, "USD_X", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });
    }

    @Test
    public void shouldThrowException_WhenAllOrderFieldInvalid() {

        OrderItem orderItemTea =
                getOrderItem("Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order invalidOrderDto = getOrder(null, "USD_X", BigDecimal.valueOf(-150), null, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });
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

        Order invalidOrderDto = getOrder(1L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });
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

        Order invalidOrderDto = getOrder(1L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });
    }

    @Test
    public void shouldThrowException_WhenQuantityIsZero() {

        OrderItem orderItemTea =
                getOrderItem("Tea",
                        0, BigDecimal.valueOf(-50), BigDecimal.valueOf(100));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",0, BigDecimal.valueOf(50), BigDecimal.valueOf(50));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order invalidOrderDto = getOrder(1L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });
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

        Order invalidOrderDto = getOrder(1L, "USD", BigDecimal.valueOf(150), OrderStatus.CREATED, orderItems);

        assertThrows(ConstraintViolationException.class, () -> {
            orderService.save(invalidOrderDto);
        });
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
