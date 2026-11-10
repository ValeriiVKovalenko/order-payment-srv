package com.valerii.order.exception;

import com.valerii.order.model.enums.OrderStatus;

public class InvalidOrderUpdateStatusException extends RuntimeException {
    public InvalidOrderUpdateStatusException(OrderStatus oldStatus, OrderStatus newStatus) {
        super("Can not update status from `" + oldStatus + " to `" + newStatus + "`");
    }
}
