package com.valerii.order.controller;

import com.valerii.order.mapper.OrderRequestMapper;
import com.valerii.order.mapper.OrderResponseMapper;
import com.valerii.order.model.CreateOrderCommand;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderSummaryDto;
import com.valerii.order.model.request.OrderRequestDto;
import com.valerii.order.model.response.order.OrderResponseDto;
import com.valerii.order.model.response.page.PageResponseDto;
import com.valerii.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
            @Max(value = 100, message = "size must be less than 101") Integer size) {

        PageResponseDto<OrderSummaryDto> orders = orderService.findAll(page, size);
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
}
