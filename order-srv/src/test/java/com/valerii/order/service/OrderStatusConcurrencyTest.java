package com.valerii.order.service;

import com.valerii.order.config.TestcontainersConfiguration;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.model.request.UpdateStatusRequestDto;
import com.valerii.order.repository.order.OrderRepository;
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
public class OrderStatusConcurrencyTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    PlatformTransactionManager txManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ObjectMapper objectMapper;

    private Long orderId;

    @BeforeEach
    void setUp() {
        String sql = """
                INSERT INTO orders (customer_id, status, total_amount, currency)
                VALUES (501, 'CREATED', 400.00, 'USD')
                RETURNING id
                """;
        orderId = jdbcTemplate.queryForObject(sql, Long.class);
    }

    @AfterEach
    void tearDown() {
        String sql = "DELETE FROM orders WHERE id = ?";
        jdbcTemplate.update(sql, orderId);
    }

    @Test
    void updateStatus_concurrentRequest_ShouldWaitForLockAndReturn409() throws Exception {
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> first = executor.submit(() ->
                    new TransactionTemplate(txManager).executeWithoutResult(tx -> {
                        orderRepository.findStatus(orderId);
                        lockAcquired.countDown();
                        awaitUnchecked(releaseLock);
                        orderRepository.updateStatus(orderId, OrderStatus.CANCELLED);
                    }));

            assertThat(lockAcquired.await(5, SECONDS)).isTrue();

            UpdateStatusRequestDto requestDto = new UpdateStatusRequestDto("PROCESSING");
            Future<Integer> second = executor.submit(() ->
                    mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/orders/{id}/status", orderId)
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
                    "SELECT status FROM orders WHERE id = ?", String.class, orderId);
            assertThat(finalStatus).isEqualTo("CANCELLED");
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
