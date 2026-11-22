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
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
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
        when(paymentCommandMapper.toCommand(any(CreatePaymentRequestDto.class))).thenReturn(createPaymentCommand);

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
                .andExpect(jsonPath("$.amount", comparesEqualTo(250.0)))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value(PaymentStatus.PENDING.name()))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-01-01T00:00:00Z"));
    }

    @Test
    void save_shouldReturnBadRequest_whenOrderIdIsNull() throws Exception {
        CreatePaymentRequestDto request = aCreatePaymentRequest().build();
        String errorMessage = "orderId: must not be null";
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

    @ParameterizedTest(name = "orderId={0} should be rejected")
    @CsvSource({
            "-1",
            "0"
    })
    void save_shouldReturnBadRequest_whenOrderIdIsNotPositive(String orderId) throws Exception {
        CreatePaymentRequestDto request = aCreatePaymentRequest()
                .orderId(Long.valueOf(orderId))
                .build();
        String errorMessage = "orderId: must be more than 0";
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
        String errorMessage = "amount: must not be null";
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

    @ParameterizedTest(name = "amount={0} should be rejected")
    @CsvSource({
            "12345678910121316.99",
            "12312.993",
            "23.9991"
    })
    void save_shouldReturnBadRequest_whenAmountExceedsAllowedPrecision(String amount) throws Exception {
        Long orderId = 100L;

        CreatePaymentRequestDto request = aCreatePaymentRequest()
                .orderId(orderId)
                .amount(new BigDecimal(amount))
                .build();
        String errorMessage = "amount: must fit NUMERIC(15,2): at most 13 integer digits and 2 decimal places";
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

    @ParameterizedTest(name = "amount={0} should be rejected")
    @CsvSource({
            "0",
            "-1"
    })
    void save_shouldReturnBadRequest_whenAmountIsNotPositive(String amount) throws Exception {
        Long orderId = 100L;

        CreatePaymentRequestDto request = aCreatePaymentRequest()
                .orderId(orderId)
                .amount(new BigDecimal(amount))
                .build();
        String errorMessage = "amount: must be more than 0";
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
        String errorMessage = "currency: must be exactly 3 uppercase letters (e.g., USD)";
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
        String errorMessage = "currency: must not be null";
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
