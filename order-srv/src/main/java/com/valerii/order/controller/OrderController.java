package com.valerii.order.controller;

import com.valerii.order.mapper.OrderRequestMapper;
import com.valerii.order.mapper.OrderResponseMapper;
import com.valerii.order.model.CreateOrderCommand;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderSummaryDto;
import com.valerii.order.model.request.OrderRequestDto;
import com.valerii.order.model.request.UpdateStatusRequestDto;
import com.valerii.order.model.request.filter.OrderFilterRequestDto;
import com.valerii.order.model.response.order.OrderResponseDto;
import com.valerii.order.model.response.page.PageResponseDto;
import com.valerii.order.model.response.status.OrderStatusHistoryResponse;
import com.valerii.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;
    private final OrderResponseMapper orderResponseMapper;
    private final OrderRequestMapper orderRequestMapper;

    @GetMapping(value = "/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id){
        Order order = orderService.findById(id);
        return ResponseEntity.ok(orderResponseMapper.toResponseDto(order));
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<OrderSummaryDto>> getOrders(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be non-negative value") Integer page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size must be more than 0")
            @Max(value = 100, message = "size must be less than 101") Integer size,
            @RequestParam(required = false) @Min(value = 1, message = "Customer id must be more than 0") Long customerId,
            @RequestParam(required = false) String status) {

        OrderFilterRequestDto filter = OrderFilterRequestDto.builder()
                .customerId(customerId)
                .status(status)
                .build();
        PageResponseDto<OrderSummaryDto> orders = orderService.findAll(filter, page, size);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> saveOrder(@RequestBody @Valid OrderRequestDto orderRequestDto) {
        CreateOrderCommand createOrderCommand = orderRequestMapper.toCreateOrderCommand(orderRequestDto);
        Order save = orderService.save(createOrderCommand);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(save.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(orderResponseMapper.toResponseDto(save));
    }

    @PatchMapping(value = "/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateStatusRequestDto requestDto) {
        orderService.updateStatus(id, requestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/status-history")
    public ResponseEntity<List<OrderStatusHistoryResponse>> getOrderStatusHistory(@PathVariable Long id) {
        List<OrderStatusHistoryResponse> orderStatusHistory = orderService.findOrderStatusHistory(id);

        return ResponseEntity.ok(orderStatusHistory);
    }
}
