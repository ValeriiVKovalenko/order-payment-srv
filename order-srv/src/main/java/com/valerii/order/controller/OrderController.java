package com.valerii.order.controller;

import com.valerii.order.mapper.OrderMapper;
import com.valerii.order.model.Order;
import com.valerii.order.model.response.order.OrderResponseDto;
import com.valerii.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @GetMapping(value = "/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id){
        Order order = orderService.findById(id);
        return ResponseEntity.ok(orderMapper.toResponseDto(order));
    }
}
