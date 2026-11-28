package com.valerii.payment.exception;

import com.valerii.payment.model.enums.PaymentStatus;

public class InvalidPaymentUpdateStatusException extends RuntimeException {
    public InvalidPaymentUpdateStatusException(PaymentStatus oldStatus, PaymentStatus newStatus) {
        super("Can not update status from '" + oldStatus + "' to '" + newStatus + "'");
    }
}
