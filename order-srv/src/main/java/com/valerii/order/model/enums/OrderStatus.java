package com.valerii.order.model.enums;

import com.valerii.order.exception.InvalidOrderUpdateStatusException;

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

    public static boolean canTransitionTo(OrderStatus oldStatus, OrderStatus newStatus) {
        boolean canTransition = true;
        switch (oldStatus) {
            case CREATED:
                if (newStatus == COMPLETED) {
                    throw new InvalidOrderUpdateStatusException(oldStatus, newStatus);
                }
                break;
            case PROCESSING:
                if (newStatus == CREATED) {
                    throw new InvalidOrderUpdateStatusException(oldStatus, newStatus);
                }
                break;
            case COMPLETED, CANCELLED:
                throw new InvalidOrderUpdateStatusException(oldStatus, newStatus);
        }
        return canTransition;
    }
}
