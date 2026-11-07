package com.valerii.order.exception;

public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(String value) {
        super("Unknown order status: " + value);
    }
}
