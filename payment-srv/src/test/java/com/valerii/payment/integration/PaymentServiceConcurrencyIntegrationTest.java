package com.valerii.payment.integration;

import com.valerii.payment.config.TestcontainersConfiguration;
import com.valerii.payment.model.enums.PaymentStatus;
import com.valerii.payment.model.request.UpdateStatusRequestDto;
import com.valerii.payment.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class PaymentServiceConcurrencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    PlatformTransactionManager txManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ObjectMapper objectMapper;

    private Long id;


    @BeforeEach
    void setUp() {
        String sql = """
                INSERT INTO payments (order_id, status, amount, currency)
                VALUES (501, 'PENDING', 400.00, 'USD')
                RETURNING id
                """;
        id = jdbcTemplate.queryForObject(sql, Long.class);
    }

    @AfterEach
    void tearDown() {
        String sql = "DELETE FROM payments WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Test
    void updateStatus_concurrentRequest_ShouldWaitForLockAndReturn409() throws Exception {
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> first = executor.submit(() ->
                    new TransactionTemplate(txManager).executeWithoutResult(tx -> {
                        paymentRepository.findStatusById(id);
                        lockAcquired.countDown();
                        awaitUnchecked(releaseLock);
                        paymentRepository.updateStatusById(id, PaymentStatus.SUCCEEDED.name());
                    }));

            assertThat(lockAcquired.await(5, SECONDS)).isTrue();

            UpdateStatusRequestDto requestDto = new UpdateStatusRequestDto("FAILED");
            Future<Integer> second = executor.submit(() ->
                    mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/payments/{id}/status", id)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto)))
                            .andReturn().getResponse().getStatus());

            await().atMost(5, SECONDS).untilAsserted(() ->
                    assertThat(jdbcTemplate.queryForObject(
                            "SELECT count(*) FROM pg_stat_activity WHERE wait_event_type = 'Lock'",
                            Integer.class)).isEqualTo(1));

            assertThat(second.isDone()).isFalse();

            releaseLock.countDown();
            first.get(5, SECONDS);

            assertThat(second.get(5, SECONDS)).isEqualTo(409);

            String finalStatus = jdbcTemplate.queryForObject(
                    "SELECT status FROM payments WHERE id = ?", String.class, id);
            assertThat(finalStatus).isEqualTo("SUCCEEDED");
        } finally {
            releaseLock.countDown();
            executor.shutdownNow();
        }
    }

    private static void awaitUnchecked(CountDownLatch latch) {
        try {
            if (!latch.await(10, SECONDS)) {
                throw new IllegalStateException("Timeout waiting for latch");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
