package com.valerii.order.controller;

import com.valerii.order.mapper.OrderRequestMapper;
import com.valerii.order.mapper.OrderResponseMapper;
import com.valerii.order.model.CreateOrderCommand;
import com.valerii.order.model.Order;
import com.valerii.order.model.request.OrderRequestDto;
import com.valerii.order.model.response.order.OrderResponseDto;
import com.valerii.order.service.OrderService;
import jakarta.validation.Valid;
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
