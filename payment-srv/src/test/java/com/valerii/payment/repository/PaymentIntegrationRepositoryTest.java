package com.valerii.payment.repository;

import com.valerii.payment.config.TestcontainersConfiguration;
import com.valerii.payment.model.domen.Payment;
import com.valerii.payment.model.entity.PaymentEntity;
import com.valerii.payment.model.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.valerii.payment.model.PaymentTestData.aPaymentDomain;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class PaymentIntegrationRepositoryTest {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void cleanUp() {
        jdbc.execute("TRUNCATE TABLE payments RESTART IDENTITY CASCADE");
    }

    @Test
    void save_shouldGenerateId() {
        //given
        Long orderId = 100L;
        Payment payment = aPaymentDomain().orderId(orderId).build();
        assertThat(payment.getId()).isNull();

        //when
        PaymentEntity saved = paymentRepository.save(payment);

        //then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void save_shouldSaveAndReturnPaymentEntity() {
        //given
        Long orderId = 100L;
        Payment payment = aPaymentDomain().orderId(orderId).build();

        //when
        PaymentEntity saved = paymentRepository.save(payment);

        //then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        assertEquals(payment.getOrderId(), saved.getOrderId());
        assertEquals(0, payment.getAmount().compareTo(saved.getAmount()));
        assertEquals(payment.getCurrency(), saved.getCurrency());
        assertEquals(payment.getStatus().name(), saved.getStatus());
    }

    @Test
    void findById_shouldReturnPayment_whenExists() {
        //given

        Long orderId = 100L;
        Payment payment = aPaymentDomain().orderId(orderId).build();
        PaymentEntity saved = paymentRepository.save(payment);
        Long paymentId = saved.getId();

        //when
        PaymentEntity result = paymentRepository.findById(paymentId).orElseThrow();

        //then
        assertThat(result).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        assertEquals(paymentId, saved.getId());
        assertEquals(payment.getOrderId(), saved.getOrderId());
        assertEquals(0, payment.getAmount().compareTo(saved.getAmount()));
        assertEquals(payment.getCurrency(), saved.getCurrency());
        assertEquals(payment.getStatus().name(), saved.getStatus());
    }

    @Test
    void findById_shouldReturnEmptyPayment_whenNotExists() {
        //given
        Long invalidPaymentId = -1L;

        //when
        Optional<PaymentEntity> result = paymentRepository.findById(invalidPaymentId);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByOrderId_shouldReturnAllPayments_whenMultiplePaymentsExist() {
        //given
        Long orderId = 1L;
        Payment first = aPaymentDomain().orderId(orderId).amount(new BigDecimal("200.00")).status(PaymentStatus.PENDING).build();
        paymentRepository.save(first);
        Payment second = aPaymentDomain().orderId(orderId).amount(new BigDecimal("50.00")).status(PaymentStatus.SUCCEEDED).build();
        paymentRepository.save(second);

        //when
        List<PaymentEntity> resultList = paymentRepository.findAllByOrderId(orderId);

        //then
        assertThat(resultList).hasSize(2);
        assertThat(resultList).extracting(
                        PaymentEntity::getId,
                        PaymentEntity::getOrderId,
                        PaymentEntity::getStatus,
                        PaymentEntity::getAmount,
                        PaymentEntity::getCurrency)
                .containsExactly(
                        tuple(2L, orderId, PaymentStatus.SUCCEEDED.name(), new BigDecimal("50.00"), "USD"),
                        tuple(1L, orderId, PaymentStatus.PENDING.name(), new BigDecimal("200.00"), "USD")
                );
    }

    @Test
    void findAllByOrderId_shouldReturnEmptyList_whenMultiplePaymentsNotExist() {
        //given
        Long orderId = 1L;

        //when
        List<PaymentEntity> resultList = paymentRepository.findAllByOrderId(orderId);

        //then
        assertThat(resultList).isEmpty();
    }
}
