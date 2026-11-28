package com.valerii.payment.exception;

public class InvalidPaymentStatusException extends RuntimeException {
    public InvalidPaymentStatusException(String status) {
        super("Status '" + status + "' is unknown");
    }
}
