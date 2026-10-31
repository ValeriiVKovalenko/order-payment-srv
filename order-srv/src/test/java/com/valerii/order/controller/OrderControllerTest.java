package com.valerii.order.controller;

import com.valerii.order.exception.OrderNotFoundException;
import com.valerii.order.mapper.OrderResponseMapper;
import com.valerii.order.model.Order;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.model.response.order.OrderResponseDto;
import com.valerii.order.model.response.orderitem.OrderItemResponseDto;
import com.valerii.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderResponseMapper orderResponseMapper;

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
}
