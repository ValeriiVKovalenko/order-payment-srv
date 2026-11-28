package com.valerii.payment.service;

import com.valerii.payment.exception.DataConsistencyException;
import com.valerii.payment.exception.InvalidPaymentStatusException;
import com.valerii.payment.exception.InvalidPaymentUpdateStatusException;
import com.valerii.payment.exception.PaymentNotFoundException;
import com.valerii.payment.mapper.PaymentDomainMapper;
import com.valerii.payment.model.command.CreatePaymentCommand;
import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.entity.PaymentEntity;
import com.valerii.payment.model.enums.PaymentStatus;
import com.valerii.payment.model.request.UpdateStatusRequestDto;
import com.valerii.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static com.valerii.payment.model.PaymentTestData.*;
import static java.util.Collections.emptyList;
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

    @Test
    void findAll_shouldReturnPaymentsList_whenOrderIdIsValid() {
        Long orderId = 100L;
        PaymentEntity firstPaymentEntity = aPaymentEntity()
                .id(1L)
                .orderId(orderId)
                .amount(new BigDecimal("300.00"))
                .status(PaymentStatus.PENDING.name())
                .currency("USD")
                .createdAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .build();
        PaymentEntity secondPaymentEntity = aPaymentEntity()
                .id(2L)
                .orderId(orderId)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.SUCCEEDED.name())
                .currency("USD")
                .createdAt(OffsetDateTime.of(2026, 11, 26, 0, 0, 0, 0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026, 11, 26, 0, 0, 0, 0, ZoneOffset.UTC))
                .build();
        when(paymentRepository.findAllByOrderId(orderId)).thenReturn(List.of(firstPaymentEntity, secondPaymentEntity));

        Payment firstPayment = aPaymentDomain()
                .id(1L)
                .orderId(100L)
                .amount(new BigDecimal("300.00"))
                .status(PaymentStatus.PENDING)
                .currency("USD")
                .createdAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .build();
        Payment secondPayment = aPaymentDomain()
                .id(2L)
                .orderId(100L)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.SUCCEEDED)
                .currency("USD")
                .createdAt(OffsetDateTime.of(2026, 11, 26, 0, 0, 0, 0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026, 11, 26, 0, 0, 0, 0, ZoneOffset.UTC))
                .build();
        when(paymentDomainMapper.toDomainList(List.of(firstPaymentEntity, secondPaymentEntity))).thenReturn(List.of(firstPayment, secondPayment));

        List<Payment> result = paymentService.findAll(orderId);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);

        Payment firstPaymentResult = result.stream().filter(payment -> payment.getId().equals(1L))
                .findFirst()
                .orElseThrow();

        assertThat(firstPaymentResult).isNotNull();
        assertThat(firstPayment.getId()).isEqualTo(firstPaymentResult.getId());
        assertThat(firstPayment.getOrderId()).isEqualTo(firstPaymentResult.getOrderId());
        assertThat(firstPayment.getCurrency()).isEqualTo(firstPaymentResult.getCurrency());
        assertThat(firstPayment.getStatus()).isEqualTo(firstPaymentResult.getStatus());
        assertThat(firstPayment.getCreatedAt()).isEqualTo(firstPaymentResult.getCreatedAt());
        assertThat(firstPayment.getUpdatedAt()).isEqualTo(firstPaymentResult.getUpdatedAt());
        assertThat(firstPayment.getAmount()).isEqualByComparingTo(firstPaymentResult.getAmount());

        Payment secondPaymentResult = result.stream().filter(payment -> payment.getId().equals(2L))
                .findFirst()
                .orElseThrow();

        assertThat(secondPaymentResult).isNotNull();
        assertThat(secondPayment.getId()).isEqualTo(secondPaymentResult.getId());
        assertThat(secondPayment.getOrderId()).isEqualTo(secondPaymentResult.getOrderId());
        assertThat(secondPayment.getCurrency()).isEqualTo(secondPaymentResult.getCurrency());
        assertThat(secondPayment.getStatus()).isEqualTo(secondPaymentResult.getStatus());
        assertThat(secondPayment.getCreatedAt()).isEqualTo(secondPaymentResult.getCreatedAt());
        assertThat(secondPayment.getUpdatedAt()).isEqualTo(secondPaymentResult.getUpdatedAt());
        assertThat(secondPayment.getAmount()).isEqualByComparingTo(secondPaymentResult.getAmount());
    }

    @Test
    void findAll_shouldReturnEmptyList() {
        Long orderId = 100L;
        when(paymentRepository.findAllByOrderId(orderId)).thenReturn(emptyList());

        when(paymentDomainMapper.toDomainList(emptyList())).thenReturn(emptyList());

        List<Payment> result = paymentService.findAll(orderId);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @ParameterizedTest(name = "status={0} should be rejected")
    @ValueSource(strings = {"SUCCEEDED", "FAILED"})
    void updateStatus_shouldUpdateStatus_whenPaymentExists(String status) {
        Long id = 30L;
        UpdateStatusRequestDto request = UpdateStatusRequestDto.builder()
                .status(status)
                .build();
        String statusBefore = PaymentStatus.PENDING.name();
        when(paymentRepository.findStatusById(id)).thenReturn(Optional.of(statusBefore));

        String newStatus = request.getStatus();
        doNothing().when(paymentRepository).updateStatusById(id, newStatus);

        paymentService.updateStatus(id, request);

        verify(paymentRepository).findStatusById(id);
        verify(paymentRepository).updateStatusById(id, newStatus);
    }

    @Test
    void updateStatus_shouldThrowInvalidPaymentStatusException_whenStatusIsInvalid() {
        Long id = 30L;
        UpdateStatusRequestDto request = UpdateStatusRequestDto.builder()
                .status("UNKNOWN")
                .build();
       assertThatThrownBy(() -> paymentService.updateStatus(id, request))
               .isInstanceOf(InvalidPaymentStatusException.class);

        verify(paymentRepository, never()).findStatusById(id);
        verify(paymentRepository, never()).updateStatusById(anyLong(), anyString());
    }

    @Test
    void updateStatus_shouldThrowPaymentNotFoundException_whenStatusDoesNotExists() {
        Long id = 30L;
        UpdateStatusRequestDto request = UpdateStatusRequestDto.builder()
                .status("PENDING")
                .build();
        when(paymentRepository.findStatusById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.updateStatus(id, request))
                .isInstanceOf(PaymentNotFoundException.class);

        verify(paymentRepository).findStatusById(id);
        verify(paymentRepository, never()).updateStatusById(anyLong(), anyString());
    }

    @Test
    void updateStatus_shouldThrowInvalidPaymentStatusException_whenStatusInDatabaseIsInvalid() {
        Long id = 30L;
        UpdateStatusRequestDto request = UpdateStatusRequestDto.builder()
                .status("PENDING")
                .build();
        String invalidStatus = "CREATED";
        when(paymentRepository.findStatusById(id)).thenReturn(Optional.of(invalidStatus));

        assertThatThrownBy(() -> paymentService.updateStatus(id, request))
                .isInstanceOf(DataConsistencyException.class);

        verify(paymentRepository).findStatusById(id);
        verify(paymentRepository, never()).updateStatusById(anyLong(), anyString());
    }

    @Test
    void updateStatus_shouldSkipUpdateStatus_whenOldStatusIsEqualNewStatus() {
        Long id = 30L;
        UpdateStatusRequestDto request = UpdateStatusRequestDto.builder()
                .status("SUCCEEDED")
                .build();
        String status = PaymentStatus.SUCCEEDED.name();
        when(paymentRepository.findStatusById(id)).thenReturn(Optional.of(status));

        paymentService.updateStatus(id, request);

        verify(paymentRepository).findStatusById(id);
        verify(paymentRepository, never()).updateStatusById(anyLong(), anyString());
    }

    @Test
    void updateStatus_shouldThrowInvalidPaymentUpdateStatusException_whenTransitionIsFailed() {
        Long id = 30L;
        UpdateStatusRequestDto request = UpdateStatusRequestDto.builder()
                .status("SUCCEEDED")
                .build();
        String status = PaymentStatus.FAILED.name();
        when(paymentRepository.findStatusById(id)).thenReturn(Optional.of(status));

        assertThatThrownBy(() -> paymentService.updateStatus(id, request))
                .isInstanceOf(InvalidPaymentUpdateStatusException.class);

        verify(paymentRepository).findStatusById(id);
        verify(paymentRepository, never()).updateStatusById(anyLong(), anyString());
    }
}
