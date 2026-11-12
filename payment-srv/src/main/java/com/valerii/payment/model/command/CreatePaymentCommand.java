package com.valerii.payment.model.command;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@RequiredArgsConstructor
public class CreatePaymentCommand {
    @NotNull(message = "Order id must not be null")
    @Min(value = 1, message = "Order id must be more than 0")
    private final Long orderId;

    @NotNull(message = "Amount must not be null")
    @Min(value = 1, message = "Amount must be more than 0")
    @Digits(fraction = 2, integer = 13, message = "Amount must fit NUMERIC(15,2): at most 13 integer digits and 2 decimal places")
    private final BigDecimal amount;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be exactly 3 uppercase letters (e.g., USD)")
    private final String currency;
}
