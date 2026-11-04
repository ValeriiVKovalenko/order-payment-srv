package com.valerii.order.exception;

import com.valerii.order.model.Order;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

public class DomainInvariantViolationException extends RuntimeException {
    public DomainInvariantViolationException(Set<ConstraintViolation<Order>> violations) {
    }
}
