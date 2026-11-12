package com.valerii.payment.service;

import com.valerii.payment.mapper.PaymentDomainMapper;
import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.entity.PaymentEntity;
import com.valerii.payment.model.request.PaymentRequestDto;
import com.valerii.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDomainMapper paymentDomainMapper;

    @Override
    public Payment save(PaymentRequestDto request) {
        PaymentEntity payment = paymentRepository.save(request);

        return paymentDomainMapper.toDomain(payment);
    }
}
