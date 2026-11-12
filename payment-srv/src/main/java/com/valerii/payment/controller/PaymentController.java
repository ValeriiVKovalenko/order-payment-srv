package com.valerii.payment.controller;

import com.valerii.payment.mapper.PaymentResponseMapper;
import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.request.PaymentRequestDto;
import com.valerii.payment.model.response.PaymentResponseDto;
import com.valerii.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentResponseMapper paymentResponseMapper;

    @PostMapping
    ResponseEntity<PaymentResponseDto> save(@RequestBody @Valid PaymentRequestDto requestDto) {
        Payment payment = paymentService.save(requestDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(payment.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(paymentResponseMapper.toResponseDto(payment));
    }
}
