package com.valerii.payment.integration;

import com.valerii.payment.config.TestcontainersConfiguration;
import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.entity.PaymentEntity;
import com.valerii.payment.model.enums.PaymentStatus;
import com.valerii.payment.model.request.CreatePaymentRequestDto;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Map;

import static com.valerii.payment.model.PaymentTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("TRUNCATE TABLE payments RESTART IDENTITY CASCADE");
    }

    @Test
    void save_shouldReturnPaymentResponse_whenRequestIsValid() throws Exception {
        Long orderId = 100L;
        BigDecimal amount = new BigDecimal("250.00");
        CreatePaymentRequestDto request = aCreatePaymentRequest()
                .orderId(orderId)
                .amount(amount)
                .build();

        String sqlCount = "SELECT count(*) FROM payments WHERE order_id = ?";
        Long before = jdbcTemplate.queryForObject(sqlCount, Long.class, orderId);
        assertThat(before).isZero();

        MvcResult result = mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))

                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value(PaymentStatus.PENDING.name()))
                .andExpect(jsonPath("$.amount", comparesEqualTo(250.0)))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        long idFromResponse = objectMapper.readTree(body).get("id").asLong();
        String location = result.getResponse().getHeader("Location");

        assertThat(location).endsWith("/api/v1/payments/" + idFromResponse);

        PaymentEntity paymentEntity = paymentRepository.findById(idFromResponse)
                .orElseThrow();

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT id, order_id, amount, currency, status, created_at, updated_at FROM payments");
        assertThat(row)
                .containsEntry("id", paymentEntity.getId())
                .containsEntry("status", PaymentStatus.PENDING.name())
                .containsEntry("order_id", orderId)
                .containsEntry("currency", "USD");
        assertThat((BigDecimal) row.get("amount")).isEqualByComparingTo(amount);
        assertThat(row.get("created_at")).isNotNull();
        assertThat(row.get("updated_at")).isNotNull();
    }

    @Test
    void findById_shouldReturnPaymentResponse_IdIsValid() throws Exception {
        Payment paymentDomain = aPaymentDomain()
                .orderId(100L)
                .amount(new BigDecimal("300.00"))
                .build();
        PaymentEntity save = paymentRepository.save(paymentDomain);
        Long id = save.getId();

        String sqlCount = "SELECT count(*) FROM payments WHERE id = ?";
        Long before = jdbcTemplate.queryForObject(sqlCount, Long.class, id);
        assertThat(before).isOne();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments/{id}", id))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.orderId").value(100L))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value(PaymentStatus.PENDING.name()))
                .andExpect(jsonPath("$.amount", comparesEqualTo(300.0)))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn();
    }

    @Test
    @Sql("/sql/payments/payments-50.sql")
    void findAll_shouldReturnPaymentListResponse_whenOrderIdIsValid() throws Exception {
        Long orderId = 20L;
        String sqlCount = "SELECT count(*) FROM payments WHERE order_id = ?";
        Long before = jdbcTemplate.queryForObject(sqlCount, Long.class, orderId);
        assertThat(before).isEqualTo(2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments")
                        .param("orderId", orderId.toString()))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.[0].id").value(30))
                .andExpect(jsonPath("$.[0].orderId").value(orderId))
                .andExpect(jsonPath("$.[0].currency").value("EUR"))
                .andExpect(jsonPath("$.[0].status").value(PaymentStatus.SUCCEEDED.name()))
                .andExpect(jsonPath("$.[0].amount", comparesEqualTo(252.40)))
                .andExpect(jsonPath("$.[0].createdAt").exists())
                .andExpect(jsonPath("$.[0].updatedAt").exists())

                .andExpect(jsonPath("$.[1].id").value(29))
                .andExpect(jsonPath("$.[1].orderId").value(orderId))
                .andExpect(jsonPath("$.[1].currency").value("EUR"))
                .andExpect(jsonPath("$.[1].status").value(PaymentStatus.FAILED.name()))
                .andExpect(jsonPath("$.[1].amount", comparesEqualTo(252.40)))
                .andExpect(jsonPath("$.[1].createdAt").exists())
                .andExpect(jsonPath("$.[1].updatedAt").exists());
    }
}
