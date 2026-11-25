package com.valerii.payment.service;

import com.valerii.payment.exception.PaymentNotFoundException;
import com.valerii.payment.mapper.PaymentDomainMapper;
import com.valerii.payment.model.command.CreatePaymentCommand;
import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.entity.PaymentEntity;
import com.valerii.payment.model.enums.PaymentStatus;
import com.valerii.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.valerii.payment.model.PaymentTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentDomainMapper paymentDomainMapper;

    @Test
    void save_shouldReturnPayment_whenRequestIsValid() {
        Long orderId = 100L;
        BigDecimal amount = new BigDecimal("50.00");
        String currency = "USD";
        CreatePaymentCommand createPaymentCommand = aCreatePaymentCommand()
                .orderId(orderId)
                .amount(amount)
                .currency(currency)
                .build();
        PaymentEntity entity = aPaymentEntity()
                .id(1L)
                .orderId(orderId)
                .amount(amount)
                .currency(currency)
                .status(PaymentStatus.PENDING.name())
                .createdAt(OffsetDateTime.of(2026,1,1,0,0,0,0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026,1,1,0,0,0,0, ZoneOffset.UTC))
                .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(entity);

        Payment result = aPaymentDomain()
                .id(1L)
                .orderId(orderId)
                .amount(amount)
                .currency(currency)
                .status(PaymentStatus.PENDING)
                .createdAt(OffsetDateTime.of(2026,1,1,0,0,0,0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026,1,1,0,0,0,0, ZoneOffset.UTC))
                .build();
        when(paymentDomainMapper.toDomain(entity)).thenReturn(result);

        //when
        Payment actual = paymentService.save(createPaymentCommand);

        //then
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.PENDING);

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(result.getId());
        assertThat(actual.getOrderId()).isEqualTo(result.getOrderId());
        assertThat(actual.getCurrency()).isEqualTo(result.getCurrency());
        assertThat(actual.getStatus()).isEqualTo(result.getStatus());
        assertThat(actual.getCreatedAt()).isEqualTo(result.getCreatedAt());
        assertThat(actual.getUpdatedAt()).isEqualTo(result.getUpdatedAt());
        assertThat(actual.getAmount()).isEqualByComparingTo(result.getAmount());

        verify(paymentDomainMapper).toDomain(entity);
    }

    @Test
    void findById_shouldReturnPayment_whenIdIsValid() {
        Long id = 1L;
        PaymentEntity entity = aPaymentEntity()
                .id(id)
                .orderId(100L)
                .amount(new BigDecimal("300.00"))
                .status(PaymentStatus.PENDING.name())
                .currency("USD")
                .createdAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .build();
        when(paymentRepository.findById(id)).thenReturn(Optional.of(entity));

        Payment domain = aPaymentDomain()
                .id(id)
                .orderId(100L)
                .amount(new BigDecimal("300.00"))
                .status(PaymentStatus.PENDING)
                .currency("USD")
                .createdAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .build();
        when(paymentDomainMapper.toDomain(entity)).thenReturn(domain);

        Payment result = paymentService.findById(id);

        assertThat(result).isNotNull();
        assertThat(domain.getId()).isEqualTo(result.getId());
        assertThat(domain.getOrderId()).isEqualTo(result.getOrderId());
        assertThat(domain.getCurrency()).isEqualTo(result.getCurrency());
        assertThat(domain.getStatus()).isEqualTo(result.getStatus());
        assertThat(domain.getCreatedAt()).isEqualTo(result.getCreatedAt());
        assertThat(domain.getUpdatedAt()).isEqualTo(result.getUpdatedAt());
        assertThat(domain.getAmount()).isEqualByComparingTo(result.getAmount());

        verify(paymentRepository).findById(id);
        verify(paymentDomainMapper).toDomain(entity);
    }

    @Test
    void findById_shouldThrowPaymentNotFoundException() {
        Long id = 2L;

        assertThatThrownBy(() -> paymentService.findById(id))
                .isInstanceOf(PaymentNotFoundException.class);

        verify(paymentDomainMapper, never()).toDomain(any());
    }
}
