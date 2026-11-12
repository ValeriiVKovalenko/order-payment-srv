package com.valerii.payment.repository;

import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.entity.PaymentEntity;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    PaymentEntity save(Payment payment);

    Optional<PaymentEntity> findById(Long id);

    List<PaymentEntity> findAllByOrderId(Long orderId);
}
