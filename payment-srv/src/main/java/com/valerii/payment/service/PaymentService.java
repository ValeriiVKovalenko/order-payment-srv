package com.valerii.payment.service;

import com.valerii.payment.model.command.CreatePaymentCommand;
import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.request.UpdateStatusRequestDto;

import java.util.List;

public interface PaymentService {
    Payment save(CreatePaymentCommand request);

    Payment findById(Long id);

    List<Payment> findAll(Long orderId);

    void updateStatus(Long id, UpdateStatusRequestDto request);
}
