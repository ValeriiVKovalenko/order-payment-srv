package com.valerii.order.exception;

import java.math.BigDecimal;

public class OrderAmountExceededException extends RuntimeException {

    private final BigDecimal totalAmount;
    private final BigDecimal maxAmount;

    public OrderAmountExceededException(BigDecimal totalAmount, BigDecimal maxAmount) {
        super("Order total amount " + totalAmount + " exceeds maximum allowed " + maxAmount);
        this.totalAmount = totalAmount;
        this.maxAmount = maxAmount;
    }
}
