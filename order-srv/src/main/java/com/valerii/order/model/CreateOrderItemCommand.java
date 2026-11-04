package com.valerii.order.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
@RequiredArgsConstructor
@Builder
@Data
public class CreateOrderItemCommand {
    @NotBlank(message = "Product name must be not an empty line")
    @Size(max = 50, message = "Product name must have maximum 50 characters")
    private final String productName;

    @NotNull(message = "Quantity must not be null")
    @Min(value = 1, message = "Quantity must be value greater than 0")
    private final Integer quantity;

    @NotNull(message = "Unit price must not be null")
    @Min(value = 0, message = "Unit price must be non-negative value")
    @Digits(fraction = 2, integer = 13, message = "Unit price must have no more than two decimal places")
    private final BigDecimal unitPrice;
}
