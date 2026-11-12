package com.valerii.payment.model;

import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.entity.PaymentEntity;
import com.valerii.payment.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class PaymentTestData {

    private PaymentTestData() {}

    public static Payment.PaymentBuilder aPaymentDomain() {
        return Payment.builder()
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(PaymentStatus.PENDING);
    }

    public static PaymentEntity.PaymentEntityBuilder aPaymentEntity() {
        return PaymentEntity.builder()
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(PaymentStatus.PENDING.name())
                .createdAt(OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
    }
}
