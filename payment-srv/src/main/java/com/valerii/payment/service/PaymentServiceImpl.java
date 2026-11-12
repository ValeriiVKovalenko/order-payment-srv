package com.valerii.payment.service;

import com.valerii.payment.mapper.PaymentDomainMapper;
import com.valerii.payment.model.command.CreatePaymentCommand;
import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.entity.PaymentEntity;
import com.valerii.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDomainMapper paymentDomainMapper;

    @Override
    public Payment save(CreatePaymentCommand request) {
        Payment payment = Payment.create(request);
        PaymentEntity paymentEntity = paymentRepository.save(payment);

        return paymentDomainMapper.toDomain(paymentEntity);
    }
}
