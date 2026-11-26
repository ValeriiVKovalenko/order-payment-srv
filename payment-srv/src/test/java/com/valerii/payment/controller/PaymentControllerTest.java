package com.valerii.payment.controller;

import com.valerii.payment.exception.PaymentNotFoundException;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.valerii.payment.model.PaymentTestData.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.*;
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

    @ParameterizedTest(name = "paymentId={0} should be rejected")
    @ValueSource(longs = {0L, -1L})
    void findById_shouldReturnBadRequest_whenIdIsNotPositive(Long id) throws Exception {
        String errorMessage = "id: must be more than 0";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/payments/" + id))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void findById_shouldReturnPayment_whenIdIsValid() throws Exception {
        Long id = 1L;
        Payment domain = aPaymentDomain()
                .id(id)
                .orderId(100L)
                .amount(new BigDecimal("300.00"))
                .status(PaymentStatus.PENDING)
                .currency("USD")
                .createdAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .build();
        when(paymentService.findById(id)).thenReturn(domain);

        PaymentResponseDto response = aPaymentResponseDto()
                .id(id)
                .orderId(100L)
                .amount(new BigDecimal("300.00"))
                .status(PaymentStatus.PENDING)
                .currency("USD")
                .createdAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .build();
        when(paymentResponseMapper.toResponseDto(domain)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments/{id}", id))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.orderId").value(100L))
                .andExpect(jsonPath("$.amount", comparesEqualTo(300.0)))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value(PaymentStatus.PENDING.name()))
                .andExpect(jsonPath("$.createdAt").value("2026-11-25T00:00:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-11-25T00:00:00Z"));
    }

    @Test
    void findById_shouldReturnNotFound_whenIdIsNotExist() throws Exception {
        Long id = 350L;
        when(paymentService.findById(id)).thenThrow(new PaymentNotFoundException(id));
        String errorMessage = "Payment with id `" + id + "` not found";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/payments/" + id))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void findAll_shouldReturnPaymentList_whenOrderIdIsValid() throws Exception {

        Long orderId = 100L;
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
        when(paymentService.findAll(orderId)).thenReturn(List.of(firstPayment, secondPayment));

        PaymentResponseDto firstResponse = aPaymentResponseDto()
                .id(1L)
                .orderId(100L)
                .amount(new BigDecimal("300.00"))
                .status(PaymentStatus.PENDING)
                .currency("USD")
                .createdAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026, 11, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .build();
        PaymentResponseDto secondResponse = aPaymentResponseDto()
                .id(2L)
                .orderId(100L)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.SUCCEEDED)
                .currency("USD")
                .createdAt(OffsetDateTime.of(2026, 11, 26, 0, 0, 0, 0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026, 11, 26, 0, 0, 0, 0, ZoneOffset.UTC))
                .build();
        when(paymentResponseMapper.toResponseDtoList(List.of(firstPayment, secondPayment)))
                .thenReturn(List.of(firstResponse, secondResponse));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments")
                        .param("orderId", "100"))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].orderId").value(100L))
                .andExpect(jsonPath("$.[0].amount", comparesEqualTo(300.0)))
                .andExpect(jsonPath("$.[0].currency").value("USD"))
                .andExpect(jsonPath("$.[0].status").value(PaymentStatus.PENDING.name()))
                .andExpect(jsonPath("$.[0].createdAt").value("2026-11-25T00:00:00Z"))
                .andExpect(jsonPath("$.[0].updatedAt").value("2026-11-25T00:00:00Z"))

                .andExpect(jsonPath("$.[1].id").value(2))
                .andExpect(jsonPath("$.[1].orderId").value(100L))
                .andExpect(jsonPath("$.[1].amount", comparesEqualTo(100.0)))
                .andExpect(jsonPath("$.[1].currency").value("USD"))
                .andExpect(jsonPath("$.[1].status").value(PaymentStatus.SUCCEEDED.name()))
                .andExpect(jsonPath("$.[1].createdAt").value("2026-11-26T00:00:00Z"))
                .andExpect(jsonPath("$.[1].updatedAt").value("2026-11-26T00:00:00Z"));
    }

    @Test
    void findAll_shouldReturnBadRequest_whenParamMiss() throws Exception {
        String errorMessage = "Required parameter 'orderId' is missing";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/payments"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void findAll_shouldReturnEmptyPaymentList() throws Exception {
        Long orderId = 100L;
        when(paymentService.findAll(orderId)).thenReturn(emptyList());

        when(paymentResponseMapper.toResponseDtoList(emptyList())).thenReturn(emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments")
                        .param("orderId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @ParameterizedTest(name = "orderId={0} should be rejected")
    @ValueSource(longs = {-1L, 0L})
    void findAll_shouldReturnBadRequest_whenOrderIdIsNotPositive(Long orderId) throws Exception {
        String errorMessage = "orderId: must be more than 0";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments")
                        .param("orderId", orderId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/payments"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @ParameterizedTest(name = "orderId={0} should be rejected")
    @ValueSource(strings = {"abc", "parameter"})
    void findAll_shouldReturnBadRequest_whenOrderIdIsInvalid(String orderId) throws Exception {
        String errorMessage = "Invalid value `%s` for parameter `%s`".formatted(orderId, "orderId");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments")
                        .param("orderId", orderId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/payments"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
