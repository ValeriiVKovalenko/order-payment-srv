package com.valerii.payment.model.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@RequiredArgsConstructor
public class CreatePaymentRequestDto {

    @NotNull(message = "must not be null")
    @Positive(message = "must be more than 0")
    private final Long orderId;

    @NotNull(message = "must not be null")
    @Positive(message = "must be more than 0")
    @Digits(fraction = 2, integer = 13, message = "must fit NUMERIC(15,2): at most 13 integer digits and 2 decimal places")
    private final BigDecimal amount;

    @NotNull(message = "must not be null")
    @Pattern(regexp = "^[A-Z]{3}$", message = "must be exactly 3 uppercase letters (e.g., USD)")
    private final String currency;
}