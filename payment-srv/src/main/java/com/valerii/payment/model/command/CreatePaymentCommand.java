package com.valerii.payment.model.command;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@RequiredArgsConstructor
public class CreatePaymentCommand {
    private final Long orderId;
    private final BigDecimal amount;
    private final String currency;
}
