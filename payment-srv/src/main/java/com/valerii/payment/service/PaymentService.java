package com.valerii.payment.service;

import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.request.PaymentRequestDto;

public interface PaymentService {
    Payment save(PaymentRequestDto request);
}
