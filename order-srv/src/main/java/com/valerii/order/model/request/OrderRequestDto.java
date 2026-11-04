package com.valerii.order.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


import java.util.List;

@Builder
@RequiredArgsConstructor
@Data
public class OrderRequestDto {
    @NotNull(message = "Customer id must not be null")
    @Min(value = 1, message = "Customer id must be more than 0")
    private final Long customerId;

    @NotNull(message = "Currency must not be null")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be exactly 3 uppercase letters (e.g., USD)")
    private final String currency;

    @NotEmpty(message = "Order items must not be empty list")
    private final List<@Valid OrderItemRequestDto> orderItems;
}
