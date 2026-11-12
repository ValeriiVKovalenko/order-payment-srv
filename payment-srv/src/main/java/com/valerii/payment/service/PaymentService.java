package com.valerii.payment.service;

import com.valerii.payment.model.command.CreatePaymentCommand;
import com.valerii.payment.model.domain.Payment;

public interface PaymentService {
    Payment save(CreatePaymentCommand request);
}
