package com.valerii.payment.service;

import com.valerii.payment.exception.DataConsistencyException;
import com.valerii.payment.exception.PaymentNotFoundException;
import com.valerii.payment.exception.InvalidPaymentStatusException;
import com.valerii.payment.mapper.PaymentDomainMapper;
import com.valerii.payment.model.command.CreatePaymentCommand;
import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.entity.PaymentEntity;
import com.valerii.payment.model.enums.PaymentStatus;
import com.valerii.payment.model.request.UpdateStatusRequestDto;
import com.valerii.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
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

    @Override
    @Transactional
    public void updateStatus(Long id, UpdateStatusRequestDto request) {
        PaymentStatus validNewPaymentStatus = PaymentStatus.fromString(request.getStatus())
                .orElseThrow(() -> new InvalidPaymentStatusException(request.getStatus()));

        String oldStatus = paymentRepository.findStatusById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        Optional<PaymentStatus> validOldPaymentStatus = PaymentStatus.fromString(oldStatus);
        if (validOldPaymentStatus.isEmpty()) {
            log.error("Payment with id '{}' has invalid status '{}'", id, oldStatus);
            throw new DataConsistencyException();
        }

        if (validNewPaymentStatus == validOldPaymentStatus.get()) {
            return;
        }
        PaymentStatus.validateTransitionTo(validOldPaymentStatus.get(), validNewPaymentStatus);

        paymentRepository.updateStatusById(id, validNewPaymentStatus.name());
    }
}
