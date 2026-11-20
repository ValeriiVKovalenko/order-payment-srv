package com.valerii.payment.controller;

import com.valerii.payment.exception.exceptionhandler.GlobalExceptionHandler;
import com.valerii.payment.mapper.PaymentCommandMapper;
import com.valerii.payment.mapper.PaymentResponseMapper;
import com.valerii.payment.model.command.CreatePaymentCommand;
import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.enums.PaymentStatus;
import com.valerii.payment.model.request.CreatePaymentRequestDto;
import com.valerii.payment.model.response.PaymentResponseDto;
import com.valerii.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static com.valerii.payment.model.PaymentTestData.*;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import(GlobalExceptionHandler.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private PaymentCommandMapper paymentCommandMapper;

    @MockitoBean
    private PaymentResponseMapper paymentResponseMapper;

    @Test
    void save_shouldReturnPayment_whenRequestIsValid() throws Exception {
        Long orderId = 100L;
        BigDecimal amount = new BigDecimal("250.00");
        CreatePaymentRequestDto request = aCreatePaymentRequest()
                .orderId(orderId)
                .amount(amount)
                .build();
        CreatePaymentCommand createPaymentCommand = aCreatePaymentCommand()
                .orderId(orderId)
                .amount(amount)
                .build();
        when(paymentCommandMapper.toCommand(request)).thenReturn(createPaymentCommand);

        Long paymentId = 1L;
        Payment paymentDomain = aPaymentDomain().
                id(paymentId)
                .orderId(orderId)
                .amount(amount)
                .build();
        when(paymentService.save(createPaymentCommand)).thenReturn(paymentDomain);

        PaymentResponseDto response = aPaymentResponseDto()
                .id(paymentId)
                .orderId(orderId)
                .amount(amount)
                .build();
        when(paymentResponseMapper.toResponseDto(paymentDomain)).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", (endsWith("/api/v1/payments/" + response.getId()))))

                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("amount").value("250.0"))
                .andExpect(jsonPath("currency").value("USD"))
                .andExpect(jsonPath("status").value(PaymentStatus.PENDING.name()))
                .andExpect(jsonPath("createdAt").value("2026-01-01T00:00:00Z"))
                .andExpect(jsonPath("createdAt").value("2026-01-01T00:00:00Z"));
    }

    @Test
    void save_shouldReturnBadRequest_whenOrderIdIsNull() throws Exception {
        CreatePaymentRequestDto request = aCreatePaymentRequest().build();
        String errorMessage = "Invalid value `null` for parameter `orderId`";
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/payments"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @ParameterizedTest
    @CsvSource({
            "-1",
            "0"
    })
    void save_shouldReturnBadRequest_whenOrderIdIsNotPositive(String orderId) throws Exception {
        CreatePaymentRequestDto request = aCreatePaymentRequest()
                .orderId(Long.valueOf(orderId))
                .build();
        String errorMessage = "Invalid value `" + orderId + "` for parameter `orderId`";
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/payments"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void save_shouldReturnBadRequest_whenAmountIsNull() throws Exception {
        Long orderId = 100L;
        CreatePaymentRequestDto request = aCreatePaymentRequest()
                .orderId(orderId)
                .amount(null)
                .build();
        String errorMessage = "Invalid value `null` for parameter `amount`";
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/payments"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @ParameterizedTest
    @CsvSource({
            "-1",
            "0",
            "12345678910121316.99",
            "12312.993",
            "23.9991"
    })
    void save_shouldReturnBadRequest_whenAmountIsInvalid(String amount) throws Exception {
        Long orderId = 100L;

        CreatePaymentRequestDto request = aCreatePaymentRequest()
                .orderId(orderId)
                .amount(new BigDecimal(amount))
                .build();
        String errorMessage = "Invalid value `" + amount + "` for parameter `amount`";
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/payments"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @ParameterizedTest(name = "currency={0} should be rejected")
    @ValueSource(strings = {"", "   ", "usd", "USDX", "U$D"})
    void save_shouldReturnBadRequest_whenCurrencyIsInvalid(String currency) throws Exception {
        Long orderId = 100L;

        CreatePaymentRequestDto request = aCreatePaymentRequest()
                .orderId(orderId)
                .currency(currency)
                .build();
        String errorMessage = "Invalid value `" + currency + "` for parameter `currency`";
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/payments"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void save_shouldReturnBadRequest_whenCurrencyIsNull() throws Exception {
        Long orderId = 100L;

        CreatePaymentRequestDto request = aCreatePaymentRequest()
                .orderId(orderId)
                .currency(null)
                .build();
        String errorMessage = "Invalid value `null` for parameter `currency`";
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/payments"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
