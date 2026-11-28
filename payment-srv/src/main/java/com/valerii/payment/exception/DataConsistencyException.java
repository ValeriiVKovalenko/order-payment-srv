package com.valerii.payment.exception;

public class DataConsistencyException extends RuntimeException {

    public DataConsistencyException() {
        super("An error occurred on the server side");
    }
}
