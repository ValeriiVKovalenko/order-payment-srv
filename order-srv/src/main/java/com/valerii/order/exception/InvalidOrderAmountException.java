package com.valerii.order.exception;

import java.math.BigDecimal;

public class InvalidOrderAmountException extends  RuntimeException {

    private final String field;
    private final BigDecimal totalAmount;
    private final BigDecimal maxAmount;

    public InvalidOrderAmountException(String field, BigDecimal totalAmount, BigDecimal maxAmount) {
        super(field + " " + totalAmount + " exceeds maximum allowed " + maxAmount);
        this.totalAmount = totalAmount;
        this.maxAmount = maxAmount;
        this.field = field;
    }
}
