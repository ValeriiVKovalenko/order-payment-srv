package com.valerii.payment.model.enums;

import com.valerii.payment.exception.InvalidPaymentUpdateStatusException;

import java.util.Arrays;
import java.util.Optional;

public enum PaymentStatus {
    PENDING,
    SUCCEEDED,
    FAILED;

    public static Optional<PaymentStatus> fromString(String request) {
        if (request == null) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(request.trim()))
                .findFirst();
    }

    public static void validateTransitionTo(PaymentStatus oldStatus, PaymentStatus newStatus) {
        switch (oldStatus) {
            case SUCCEEDED:
                if (newStatus == FAILED || newStatus == PENDING) {
                    throw new InvalidPaymentUpdateStatusException(oldStatus, newStatus);
                }
                break;
            case FAILED:
                if (newStatus == SUCCEEDED || newStatus == PENDING) {
                    throw new InvalidPaymentUpdateStatusException(oldStatus, newStatus);
                }
                break;
        }
    }
}
