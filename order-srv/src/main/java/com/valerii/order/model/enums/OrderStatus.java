package com.valerii.order.model.enums;

import java.util.Arrays;
import java.util.Optional;

public enum OrderStatus {
    CREATED,
    PROCESSING,
    COMPLETED,
    CANCELLED;

    public static Optional<OrderStatus> fromString(String requestStatus) {
        if (requestStatus == null) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(requestStatus.trim()))
                .findFirst();
    }
}
