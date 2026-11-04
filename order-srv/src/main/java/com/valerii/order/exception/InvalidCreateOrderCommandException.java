package com.valerii.order.exception;

import com.valerii.order.model.CreateOrderCommand;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

public class InvalidCreateOrderCommandException extends RuntimeException {

    public InvalidCreateOrderCommandException(Set<ConstraintViolation<CreateOrderCommand>> violations) {
    }
}
