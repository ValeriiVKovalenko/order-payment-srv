package com.valerii.payment.service;

import com.valerii.payment.exception.PaymentNotFoundException;
import com.valerii.payment.mapper.PaymentDomainMapper;
import com.valerii.payment.model.command.CreatePaymentCommand;
import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.entity.PaymentEntity;
import com.valerii.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDomainMapper paymentDomainMapper;

    @Override
    public Payment save(CreatePaymentCommand request) {
        Payment payment = Payment.create(request.getOrderId(), request.getAmount(), request.getCurrency());
        PaymentEntity paymentEntity = paymentRepository.save(payment);

        return paymentDomainMapper.toDomain(paymentEntity);
    }

    @Override
    public Payment findById(Long id) {
        PaymentEntity paymentEntity = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        return paymentDomainMapper.toDomain(paymentEntity);
    }

    @Override
    public List<Payment> findAll(Long orderId) {
        List<PaymentEntity> entities = paymentRepository.findAllByOrderId(orderId);
        return paymentDomainMapper.toDomainList(entities);
    }
}
