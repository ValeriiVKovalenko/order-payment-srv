package com.valerii.order.exception;

import jakarta.validation.ConstraintViolation;

import java.util.*;
import java.util.stream.Collectors;

public class ViolationException extends RuntimeException {

    private final LinkedHashMap<String, String> errors;

    public ViolationException(Set<? extends ConstraintViolation<?>> violations) {
        this(toErrors(violations));
    }

    private ViolationException(LinkedHashMap<String, String> errors) {
        super(getMessage(errors));
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return Collections.unmodifiableMap(errors);
    }

    private static LinkedHashMap<String, String> toErrors(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, b) -> a + ", " + b,
                        LinkedHashMap::new));
    }

    private static String getMessage(Map<String, String> errors) {
        return errors.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("; "));
    }
}
