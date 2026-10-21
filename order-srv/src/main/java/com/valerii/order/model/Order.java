package com.valerii.order.model;

import com.valerii.order.model.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotBlank()
    @Size(min = 3, max = 3)
    private final String currency;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    @NotNull
    private final List<@Valid OrderItem> orderItems;
}
