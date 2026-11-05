package com.valerii.order.model;

import com.valerii.order.exception.InvalidOrderAmountException;
import com.valerii.order.model.enums.OrderStatus;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class OrderFactory {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999999.99");

    public static Order createOrder(CreateOrderCommand orderCommand) {

        List<CreateOrderItemCommand> orderItems = orderCommand.getOrderItems();
        List<OrderItem> resultList = new ArrayList<>(orderItems.size());
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderItemCommand orderItem : orderItems) {
            Integer quantity = orderItem.getQuantity();
            BigDecimal unitPrice = orderItem.getUnitPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            if (lineTotal.compareTo(MAX_AMOUNT) > 0) {
                throw new InvalidOrderAmountException("Line total", lineTotal, MAX_AMOUNT);
            }
            OrderItem resultOrderItem = OrderItem.builder()
                    .productName(orderItem.getProductName())
                    .quantity(orderItem.getQuantity())
                    .unitPrice(orderItem.getUnitPrice())
                    .lineTotal(lineTotal)
                    .build();
            resultList.add(resultOrderItem);

            totalAmount = totalAmount.add(lineTotal);

            if (totalAmount.compareTo(MAX_AMOUNT) > 0) {
                throw new InvalidOrderAmountException("Total amount" ,totalAmount, MAX_AMOUNT);
            }
        }

        return Order.builder()
                .customerId(orderCommand.getCustomerId())
                .status(OrderStatus.CREATED)
                .totalAmount(totalAmount)
                .currency(orderCommand.getCurrency())
                .orderItems(resultList)
                .build();
    }
}
