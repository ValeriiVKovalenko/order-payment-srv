package com.valerii.payment.model.domain;

import com.valerii.payment.model.command.CreatePaymentCommand;
import com.valerii.payment.model.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
@RequiredArgsConstructor
public class Payment {
    private final Long id;
    private final Long orderId;
    private final BigDecimal amount;
    private final String currency;
    private final PaymentStatus status;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public static Payment create(CreatePaymentCommand command) {
        return Payment.builder()
                .orderId(command.getOrderId())
                .amount(command.getAmount())
                .currency(command.getCurrency())
                .status(PaymentStatus.PENDING)
                .build();
    }
}
