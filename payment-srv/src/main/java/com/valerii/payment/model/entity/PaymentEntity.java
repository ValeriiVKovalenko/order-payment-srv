package com.valerii.payment.model.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
@RequiredArgsConstructor
public class PaymentEntity {
    private final Long id;
    private final Long orderId;
    private final BigDecimal amount;
    private final String currency;
    private final String status;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
}
