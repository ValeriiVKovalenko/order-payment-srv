package com.valerii.order.model;

import com.valerii.order.model.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class Order {
    private final Long id;
    @NotNull(message = "Customer id must not be null")
    @Min(value = 1, message = "Customer id must be more than 0")
    private final Long customerId;

    @NotNull(message = "Status must not be null")
    private final OrderStatus status;

    @NotNull(message = "Total amount must not be null")
    @Min(value = 0, message = "Total amount must not be negative")
    private final BigDecimal totalAmount;

    @NotNull(message = "Currency must not be null")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be exactly 3 uppercase letters (e.g., USD)")
    private final String currency;

    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    @NotNull(message = "Order items must not be null")
    private final List<@Valid OrderItem> orderItems;
}
