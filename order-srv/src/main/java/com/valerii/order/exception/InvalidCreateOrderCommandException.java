package com.valerii.order.exception;

import com.valerii.order.model.CreateOrderCommand;
import jakarta.validation.ConstraintViolation;

import java.util.*;
import java.util.stream.Collectors;

public class InvalidCreateOrderCommandException extends RuntimeException {

    private final LinkedHashMap<String, String> errors;

    public InvalidCreateOrderCommandException(Set<ConstraintViolation<CreateOrderCommand>> violations) {
        this(toErrors(violations));
    }

    private InvalidCreateOrderCommandException(LinkedHashMap<String, String> errors) {
        super(buildMessage(errors));
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return Collections.unmodifiableMap(errors);
    }

    private static LinkedHashMap<String, String> toErrors(Set<ConstraintViolation<CreateOrderCommand>> violations) {
        return violations.stream()
                .sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, b) -> a + ", " + b,
                        LinkedHashMap::new));
    }

    private static String buildMessage(LinkedHashMap<String, String> erorrs) {
        return erorrs.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("; "));
    }
}
