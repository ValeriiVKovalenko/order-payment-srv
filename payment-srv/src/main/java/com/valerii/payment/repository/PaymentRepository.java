package com.valerii.payment.repository;

import com.valerii.payment.model.entity.PaymentEntity;
import com.valerii.payment.model.request.PaymentRequestDto;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    PaymentEntity save(PaymentRequestDto payment);

    Optional<PaymentEntity> findById(Long id);

    List<PaymentEntity> findAllByOrderId(Long orderId);
}
