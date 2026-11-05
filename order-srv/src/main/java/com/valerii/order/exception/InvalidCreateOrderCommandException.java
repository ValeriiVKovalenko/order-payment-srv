package com.valerii.order.exception;

import jakarta.validation.ConstraintViolation;

import java.util.*;

public class InvalidCreateOrderCommandException extends ViolationException {

    public InvalidCreateOrderCommandException(Set<? extends ConstraintViolation<?>> violations) {
        super(violations);
    }
}
