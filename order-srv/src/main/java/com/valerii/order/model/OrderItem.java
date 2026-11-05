package com.valerii.order.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@Builder
public class OrderItem {
    private final Long id;
    private final Long orderId;
    @NotBlank(message = "Product name must be not an empty line")
    @Size(max = 50, message = "Product name must have maximum 50 characters")
    private final String productName;

    @NotNull(message = "Quantity must not be null")
    @Min(value = 1, message = "Quantity must be value greater than 0")
    private final Integer quantity;

    @NotNull(message = "Unit price must not be null")
    @Min(value = 0, message = "Unit price must be non-negative value")
    @Digits(fraction = 2, integer = 13, message = "Unit price must fit NUMERIC(15,2): at most 13 integer digits and 2 decimal places")
    private final BigDecimal unitPrice;

    @NotNull(message = "Line total must not be null")
    @Min(value = 0, message = "Line total must be non-negative value")
    @Digits(fraction = 2, integer = 13, message = "Line total must fit NUMERIC(15,2): at most 13 integer digits and 2 decimal places")
    private final BigDecimal lineTotal;
}
