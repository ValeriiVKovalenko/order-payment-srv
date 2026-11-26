package com.valerii.payment.service;

import com.valerii.payment.model.command.CreatePaymentCommand;
import com.valerii.payment.model.domain.Payment;

import java.util.List;

public interface PaymentService {
    Payment save(CreatePaymentCommand request);

    Payment findById(Long id);

    List<Payment> findAll(Long orderId);
}
