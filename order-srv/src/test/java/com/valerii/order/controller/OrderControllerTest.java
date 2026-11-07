package com.valerii.order.controller;

import com.valerii.order.controller.exceptionhandler.GlobalExceptionHandler;
import com.valerii.order.exception.DomainInvariantViolationException;
import com.valerii.order.exception.InvalidOrderAmountException;
import com.valerii.order.exception.OrderNotFoundException;
import com.valerii.order.mapper.OrderRequestMapper;
import com.valerii.order.mapper.OrderResponseMapper;
import com.valerii.order.model.*;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.model.request.OrderItemRequestDto;
import com.valerii.order.model.request.OrderRequestDto;
import com.valerii.order.model.response.order.OrderResponseDto;
import com.valerii.order.model.response.orderitem.OrderItemResponseDto;
import com.valerii.order.model.response.page.PageResponseDto;
import com.valerii.order.service.OrderService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderResponseMapper orderResponseMapper;

    @MockitoBean
    private OrderRequestMapper orderRequestMapper;

    @Test
    void findById_shouldReturnOrder_whenExists() throws Exception {
        Long orderId = 100L;
        Order order = mock(Order.class);

        OrderResponseDto orderResponse = getOrderResponse(orderId, 101L, "USD", OrderStatus.CREATED, BigDecimal.valueOf(250),
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                List.of(getOrderItemResponse(1L, orderId, "Tea", 2, BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                        getOrderItemResponse(2L, orderId, "Coffee", 1, BigDecimal.valueOf(150), BigDecimal.valueOf(150))
                ));

        when(orderService.findById(orderId)).thenReturn(order);
        when(orderResponseMapper.toResponseDto(order)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.customerId").value(101L))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(BigDecimal.valueOf(250)))
                .andExpect(jsonPath("$.createdAt").value("2026-10-26T18:28:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-10-26T18:28:00Z"))

                .andExpect(jsonPath("$.orderItems", hasSize(2)))
                .andExpect(jsonPath("$.orderItems[0].id").value(1L))
                .andExpect(jsonPath("$.orderItems[0].orderId").value(orderId))
                .andExpect(jsonPath("$.orderItems[0].productName").value("Tea"))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(2))
                .andExpect(jsonPath("$.orderItems[0].unitPrice").value(BigDecimal.valueOf(50)))
                .andExpect(jsonPath("$.orderItems[0].lineTotal").value(BigDecimal.valueOf(100)))

                .andExpect(jsonPath("$.orderItems[1].id").value(2L))
                .andExpect(jsonPath("$.orderItems[1].orderId").value(orderId))
                .andExpect(jsonPath("$.orderItems[1].productName").value("Coffee"))
                .andExpect(jsonPath("$.orderItems[1].quantity").value(1))
                .andExpect(jsonPath("$.orderItems[1].unitPrice").value(BigDecimal.valueOf(150)))
                .andExpect(jsonPath("$.orderItems[1].lineTotal").value(BigDecimal.valueOf(150)));
    }

    @Test
    void findById_shouldReturnNotFound_whenOrderDoesNotExist() throws Exception {
        Long orderId = -1L;
        String errorMessage = String.format("Order with id `%d` not found", orderId);
        when(orderService.findById(orderId))
                .thenThrow(new OrderNotFoundException(errorMessage));

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value(String.format("/api/v1/orders/%d", orderId)))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void findById_shouldReturnBadRequest_whenIdIsInvalid() throws Exception {
        String errorMessage = String.format("Invalid value `%s` for parameter `%s`", "orderId", "id");
        mockMvc.perform(get("/api/v1/orders/orderId"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/orderId"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void saveOrder_shouldReturnCreated_whenIdIsValid() throws Exception {
        Long orderId = 100L;
        Long customerId = 101L;
        String currency = "USD";
        OrderRequestDto orderRequest = getOrderRequest(customerId, currency ,
                List.of(getOrderItemRequest("Tea", 2, BigDecimal.valueOf(13.44)),
                        getOrderItemRequest("Coffee", 1, BigDecimal.valueOf(26.38))));

        CreateOrderCommand createOrderCommand = getCreateOrderCommand(101L, currency,
                List.of(getCreateOrderItemCommand("Tea", 2, BigDecimal.valueOf(13.44)),
                        getCreateOrderItemCommand("Coffee", 1, BigDecimal.valueOf(26.38))));
        when(orderRequestMapper.toCreateOrderCommand(orderRequest)).thenReturn(createOrderCommand);

        Order order = getOrder(orderId, customerId, currency, BigDecimal.valueOf(53.26), OrderStatus.CREATED,
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                List.of(getOrderItem(1L, orderId, "Tea", 2, BigDecimal.valueOf(13.44),
                                BigDecimal.valueOf(26.88)),
                        getOrderItem(2L, orderId, "Coffee", 1, BigDecimal.valueOf(26.38),
                                BigDecimal.valueOf(26.38))));
        when(orderService.save(createOrderCommand)).thenReturn(order);

        OrderResponseDto orderResponse = getOrderResponse(orderId, customerId, currency, OrderStatus.CREATED, BigDecimal.valueOf(53.26),
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                List.of(getOrderItemResponse(1L, orderId, "Tea", 2, BigDecimal.valueOf(13.44), BigDecimal.valueOf(26.88)),
                        getOrderItemResponse(2L, orderId, "Coffee", 1, BigDecimal.valueOf(26.38), BigDecimal.valueOf(26.38))
                ));
        when(orderResponseMapper.toResponseDto(order)).thenReturn(orderResponse);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", (endsWith("/api/v1/orders/" + orderResponse.getId()))))
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.customerId").value(101L))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(BigDecimal.valueOf(53.26)))
                .andExpect(jsonPath("$.createdAt").value("2026-10-26T18:28:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-10-26T18:28:00Z"))

                .andExpect(jsonPath("$.orderItems", hasSize(2)))
                .andExpect(jsonPath("$.orderItems[0].id").value(1L))
                .andExpect(jsonPath("$.orderItems[0].orderId").value(orderId))
                .andExpect(jsonPath("$.orderItems[0].productName").value("Tea"))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(2))
                .andExpect(jsonPath("$.orderItems[0].unitPrice").value(BigDecimal.valueOf(13.44)))
                .andExpect(jsonPath("$.orderItems[0].lineTotal").value(BigDecimal.valueOf(26.88)))

                .andExpect(jsonPath("$.orderItems[1].id").value(2L))
                .andExpect(jsonPath("$.orderItems[1].orderId").value(orderId))
                .andExpect(jsonPath("$.orderItems[1].productName").value("Coffee"))
                .andExpect(jsonPath("$.orderItems[1].quantity").value(1))
                .andExpect(jsonPath("$.orderItems[1].unitPrice").value(BigDecimal.valueOf(26.38)))
                .andExpect(jsonPath("$.orderItems[1].lineTotal").value(BigDecimal.valueOf(26.38)));
    }

    @Test
    void saveOrder_shouldReturnBadRequest_whenOrderItemsEmptyList() throws Exception {
        Long customerId = 101L;
        String currency = "USD";
        OrderRequestDto orderRequest = getOrderRequest(customerId, currency, emptyList());
        String errorMessage = "orderItems: Order items must not be empty list";
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void saveOrder_shouldReturnBadRequest_whenWithoutProductName() throws Exception {
        Long customerId = 101L;
        String currency = "USD";
        OrderRequestDto orderRequest = getOrderRequest(customerId, currency,
                List.of(getOrderItemRequest(" ", 2, BigDecimal.valueOf(13.44)),
                        getOrderItemRequest("Coffee", 1, BigDecimal.valueOf(26.38))));
        String errorMessage = "orderItems[0].productName: Product name must be not an empty line";
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void saveOrder_shouldReturnBadRequest_whenQuantityIsZero() throws Exception {
        Long customerId = 101L;
        String currency = "USD";
        OrderRequestDto orderRequest = getOrderRequest(customerId, currency,
                List.of(getOrderItemRequest("Tea", 2, BigDecimal.valueOf(13.44)),
                        getOrderItemRequest("Coffee", 0, BigDecimal.valueOf(26.38))));
        String errorMessage = "orderItems[1].quantity: Quantity must be value greater than 0";
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @ParameterizedTest(name = "currency={0} should be rejected")
    @ValueSource(strings = {"", "   ", "usd", "USDX", "U$D"})
    void saveOrder_shouldReturnBadRequest_whenCurrencyInvalid(String currency) throws Exception {
        Long customerId = 101L;
        OrderRequestDto orderRequest = getOrderRequest(customerId, currency,
                List.of(getOrderItemRequest("Tea", 2, BigDecimal.valueOf(13.44)),
                        getOrderItemRequest("Coffee", 1, BigDecimal.valueOf(26.38))));
        String errorMessage = "currency: Currency must be exactly 3 uppercase letters (e.g., USD)";
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @ParameterizedTest(name = "customerId={0} should be rejected")
    @ValueSource(longs = {-1, 0})
    void saveOrder_shouldReturnBadRequest_whenCustomerIdIsInvalid(Long customerId) throws Exception {
        String currency = "USD";
        OrderRequestDto orderRequest = getOrderRequest(customerId, currency,
                List.of(getOrderItemRequest("Tea", 2, BigDecimal.valueOf(13.44)),
                        getOrderItemRequest("Coffee", 1, BigDecimal.valueOf(26.38))));
        String errorMessage = "customerId: Customer id must be more than 0";
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void saveOrder_shouldReturnBadRequest_whenOneOrderItemIsNull() throws Exception {
        Long customerId = 101L;
        String currency = "USD";
        OrderRequestDto orderRequest = getOrderRequest(customerId, currency ,
                Arrays.asList(getOrderItemRequest("Tea", 2, BigDecimal.valueOf(13.44)),
                        null));
        String errorMessage = "orderItems[1]: Order item must not be null";
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void saveOrder_shouldReturnBadRequest_whenOneOrderItemUnitPriceIsNegative() throws Exception {
        Long customerId = 101L;
        String currency = "USD";
        OrderRequestDto orderRequest = getOrderRequest(customerId, currency ,
                List.of(getOrderItemRequest("Tea", 2, BigDecimal.valueOf(13.44)),
                        getOrderItemRequest("Coffee", 1, BigDecimal.valueOf(-26.38))));
        String errorMessage = "orderItems[1].unitPrice: Unit price must be non-negative value";
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void saveOrder_shouldReturnUnprocessableContent_whenOrderAmountIsInvalid()  throws Exception {
        Long customerId = 101L;
        String currency = "USD";
        OrderRequestDto orderRequest = getOrderRequest(customerId, currency ,
                List.of(getOrderItemRequest("Bugatti veyron", 1, new BigDecimal("6000000000000.00")),
                        getOrderItemRequest("Bugatti divo", 1, new BigDecimal("6000000000000.00"))));

        CreateOrderCommand createOrderCommand = getCreateOrderCommand(101L, currency,
                List.of(getCreateOrderItemCommand("Bugatti veyron", 1, new BigDecimal("6000000000000.00")),
                        getCreateOrderItemCommand("Bugatti divo", 1, new BigDecimal("6000000000000.00"))));
        when(orderRequestMapper.toCreateOrderCommand(orderRequest)).thenReturn(createOrderCommand);

        when(orderService.save(any(CreateOrderCommand.class)))
                .thenThrow(new InvalidOrderAmountException(
                        "Total amount", new BigDecimal("12000000000000.00"), new BigDecimal("9999999999999.99")));

        String errorMessage = "Total amount 12000000000000.00 exceeds maximum allowed 9999999999999.99";
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void saveOrder_shouldReturnInternalServerError_whenDomainInvariantIsViolated() throws Exception {
        Long customerId = 101L;
        String currency = "USD";
        OrderRequestDto orderRequest = getOrderRequest(customerId, currency ,
                List.of(getOrderItemRequest("Bugatti veyron", 1, new BigDecimal("6000000.00")),
                        getOrderItemRequest("Bugatti divo", 1, new BigDecimal("6000000.00"))));

        CreateOrderCommand createOrderCommand = getCreateOrderCommand(101L, currency,
                List.of(getCreateOrderItemCommand("Bugatti veyron", 1, new BigDecimal("6000000.00")),
                        getCreateOrderItemCommand("Bugatti divo", 1, new BigDecimal("6000000.00"))));
        when(orderRequestMapper.toCreateOrderCommand(orderRequest)).thenReturn(createOrderCommand);
        Set<ConstraintViolation<Order>> orderViolations = getOrderViolations();

        when(orderService.save(any(CreateOrderCommand.class)))
                .thenThrow(new DomainInvariantViolationException(orderViolations));

        String errorMessage = "An unexpected error occurred. Please try again later.";
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void findAll_shouldReturnOrderList_whenParamsDefault() throws Exception {
        int page = 0;
        int size = 20;

        List<OrderSummaryDto> orderList = getOrderList();
        PageResponseDto<OrderSummaryDto> orderPageResponseDto = PageResponseDto.of(orderList, size, page, 3L);

        when(orderService.findAll(page, size)).thenReturn(orderPageResponseDto);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false))

                .andExpect(jsonPath("$.content[0].id").value(100L))
                .andExpect(jsonPath("$.content[0].currency").value("USD"))
                .andExpect(jsonPath("$.content[0].status").value("CREATED"))
                .andExpect(jsonPath("$.content[0].totalAmount").value(new BigDecimal("53.26")))
                .andExpect(jsonPath("$.content[0].createdAt").value("2026-10-26T23:28:00Z"))
                .andExpect(jsonPath("$.content[0].updatedAt").value("2026-10-26T23:28:00Z"))

                .andExpect(jsonPath("$.content[1].id").value(101L))
                .andExpect(jsonPath("$.content[1].currency").value("USD"))
                .andExpect(jsonPath("$.content[1].status").value("CREATED"))
                .andExpect(jsonPath("$.content[1].totalAmount").value(new BigDecimal("62.9")))
                .andExpect(jsonPath("$.content[1].createdAt").value("2026-10-26T22:28:00Z"))
                .andExpect(jsonPath("$.content[1].updatedAt").value("2026-10-26T22:28:00Z"))

                .andExpect(jsonPath("$.content[2].id").value(102L))
                .andExpect(jsonPath("$.content[2].currency").value("USD"))
                .andExpect(jsonPath("$.content[2].status").value("CREATED"))
                .andExpect(jsonPath("$.content[2].totalAmount").value(new BigDecimal("41.98")))
                .andExpect(jsonPath("$.content[2].createdAt").value("2026-10-26T21:28:00Z"))
                .andExpect(jsonPath("$.content[2].updatedAt").value("2026-10-26T21:28:00Z"));
    }

    @Test
    void findAll_shouldReturnEmptyPage_whenPageExceedsTotalPages() throws Exception {
        int page = Integer.MAX_VALUE;
        int size = 3;

        List<OrderSummaryDto> orderList = emptyList();
        PageResponseDto<OrderSummaryDto> orderPageResponseDto = PageResponseDto.of(orderList, size, page, 0L);

        when(orderService.findAll(page, size)).thenReturn(orderPageResponseDto);

        mockMvc.perform(get("/api/v1/orders")
                        .param("page", "2147483647")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(true))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void findAll_shouldReturnOrderList_whenParamsAreSet() throws Exception {
        int page = 0;
        int size = 3;

        List<OrderSummaryDto> orderList = getOrderList();
        PageResponseDto<OrderSummaryDto> orderPageResponseDto = PageResponseDto.of(orderList, size, page, 3L);

        when(orderService.findAll(page, size)).thenReturn(orderPageResponseDto);

        mockMvc.perform(get("/api/v1/orders")
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false))
                .andExpect(jsonPath("$.content", hasSize(3)))

                .andExpect(jsonPath("$.content[0].id").value(100L))
                .andExpect(jsonPath("$.content[0].currency").value("USD"))
                .andExpect(jsonPath("$.content[0].status").value("CREATED"))
                .andExpect(jsonPath("$.content[0].totalAmount").value(new BigDecimal("53.26")))
                .andExpect(jsonPath("$.content[0].createdAt").value("2026-10-26T23:28:00Z"))
                .andExpect(jsonPath("$.content[0].updatedAt").value("2026-10-26T23:28:00Z"))

                .andExpect(jsonPath("$.content[1].id").value(101L))
                .andExpect(jsonPath("$.content[1].currency").value("USD"))
                .andExpect(jsonPath("$.content[1].status").value("CREATED"))
                .andExpect(jsonPath("$.content[1].totalAmount").value(new BigDecimal("62.9")))
                .andExpect(jsonPath("$.content[1].createdAt").value("2026-10-26T22:28:00Z"))
                .andExpect(jsonPath("$.content[1].updatedAt").value("2026-10-26T22:28:00Z"))

                .andExpect(jsonPath("$.content[2].id").value(102L))
                .andExpect(jsonPath("$.content[2].currency").value("USD"))
                .andExpect(jsonPath("$.content[2].status").value("CREATED"))
                .andExpect(jsonPath("$.content[2].totalAmount").value(new BigDecimal("41.98")))
                .andExpect(jsonPath("$.content[2].createdAt").value("2026-10-26T21:28:00Z"))
                .andExpect(jsonPath("$.content[2].updatedAt").value("2026-10-26T21:28:00Z"));
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10",
            "0, 0",
            "0, -5",
            "0, 1000",
            "abc, 10",
            "0, xyz"
    })
    void getOrders_shouldReturn400WhenParamsAreInvalid(String page, String size) throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .param("page", page)
                        .param("size", size))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    private List<OrderSummaryDto> getOrderList () {
        List<OrderSummaryDto> orders = new ArrayList<>();
        String currency = "USD";

        OrderSummaryDto order1 = getOrderSummary(100L, 101L, currency, new BigDecimal("53.26"), OrderStatus.CREATED,
                OffsetDateTime.of(2026, 10, 26, 23, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 23, 28, 0, 0, ZoneOffset.UTC));
        orders.add(order1);
        OrderSummaryDto order2 = getOrderSummary(101L, 101L, currency, new BigDecimal("62.9"), OrderStatus.CREATED,
                OffsetDateTime.of(2026, 10, 26, 22, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 22, 28, 0, 0, ZoneOffset.UTC));
        orders.add(order2);
        OrderSummaryDto order3 = getOrderSummary(102L, 101L, currency, new BigDecimal("41.98"), OrderStatus.CREATED,
                OffsetDateTime.of(2026, 10, 26, 21, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 21, 28, 0, 0, ZoneOffset.UTC));
        orders.add(order3);

        return orders;
    }

    private OrderResponseDto getOrderResponse(Long id, Long customerId, String currency, OrderStatus status,
                                              BigDecimal totalAmount, OffsetDateTime createdAt,
                                              OffsetDateTime updatedAt, List<OrderItemResponseDto> orderItems) {
        return OrderResponseDto.builder()
                .id(id)
                .customerId(customerId)
                .currency(currency)
                .status(status)
                .totalAmount(totalAmount)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .orderItems(orderItems)
                .build();
    }

    private OrderItemResponseDto getOrderItemResponse(Long id, Long orderId, String productName, Integer quantity
            , BigDecimal unitPrice, BigDecimal lineTotal) {
        return OrderItemResponseDto.builder()
                .id(id)
                .orderId(orderId)
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .lineTotal(lineTotal)
                .build();
    }

    private CreateOrderItemCommand getCreateOrderItemCommand(String productName, Integer quantity,
                                                             BigDecimal unitPrice) {
        return CreateOrderItemCommand.builder()
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();
    }

    private CreateOrderCommand getCreateOrderCommand(Long customerId, String currency,
                                                     List<CreateOrderItemCommand> orderItemRequestDtoList) {
        return CreateOrderCommand.builder()
                .customerId(customerId)
                .currency(currency)
                .orderItems(orderItemRequestDtoList)
                .build();
    }

    private Order getOrder(Long id, Long customerId, String currency, BigDecimal totalAmount, OrderStatus status,
                           OffsetDateTime createdAt, OffsetDateTime updatedAt,
                           List<OrderItem> orderItems) {
        return Order.builder()
                .id(id)
                .customerId(customerId)
                .currency(currency)
                .totalAmount(totalAmount)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .orderItems(orderItems)
                .build();
    }

    private OrderItem getOrderItem(Long id, Long orderId, String productName, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        return OrderItem.builder()
                .id(id)
                .orderId(orderId)
                .productName(productName)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .build();
    }

    private OrderRequestDto getOrderRequest(Long customerId, String currency,
                                            List<OrderItemRequestDto> itemRequestDtoList) {
        return OrderRequestDto.builder()
                .customerId(customerId)
                .currency(currency)
                .orderItems(itemRequestDtoList)
                .build();
    }

    private OrderItemRequestDto getOrderItemRequest(String productName, Integer quantity,
                                                    BigDecimal unitPrice) {
        return OrderItemRequestDto.builder()
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();
    }

    private Set<ConstraintViolation<Order>> getOrderViolations() {
        Order invalidOrder = getOrder(10L, 101L, "USD", new BigDecimal("12000000000000.00"), OrderStatus.CREATED,
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 10, 26, 18, 28, 0, 0, ZoneOffset.UTC),
                List.of(getOrderItem(1L, 10L, "Tea", 2, new BigDecimal("13.44"),
                                new BigDecimal("26.88")),
                        getOrderItem(2L, 10L, "Coffee", 1, new BigDecimal("26.38"),
                                new BigDecimal("26.38"))));

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<Order>> violations = validator.validate(invalidOrder);
            assertFalse(violations.isEmpty());
            return violations;
        }
    }

    private OrderSummaryDto getOrderSummary(Long id, Long customerId, String currency, BigDecimal totalAmount, OrderStatus status,
                           OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        return OrderSummaryDto.builder()
                .id(id)
                .customerId(customerId)
                .currency(currency)
                .totalAmount(totalAmount)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
