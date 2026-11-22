package com.valerii.payment.integration;

import com.valerii.payment.config.TestcontainersConfiguration;
import com.valerii.payment.model.entity.PaymentEntity;
import com.valerii.payment.model.enums.PaymentStatus;
import com.valerii.payment.model.request.CreatePaymentRequestDto;
import com.valerii.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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

    @BeforeEach
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
}
