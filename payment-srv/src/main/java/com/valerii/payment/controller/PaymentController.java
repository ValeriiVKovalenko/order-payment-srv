package com.valerii.payment.controller;

import com.valerii.payment.mapper.PaymentCommandMapper;
import com.valerii.payment.mapper.PaymentResponseMapper;
import com.valerii.payment.model.command.CreatePaymentCommand;
import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.request.CreatePaymentRequestDto;
import com.valerii.payment.model.request.UpdateStatusRequestDto;
import com.valerii.payment.model.response.PaymentResponseDto;
import com.valerii.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentResponseMapper paymentResponseMapper;
    private final PaymentCommandMapper paymentCommandMapper;

    @PatchMapping("/{id}/status")
    ResponseEntity<Void> updateStatus(@PathVariable @Positive(message = "must be more than 0") Long id,
                                      @RequestBody @Valid UpdateStatusRequestDto request) {
        paymentService.updateStatus(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    ResponseEntity<List<PaymentResponseDto>> findAll(@RequestParam
                                                     @Positive(message = "must be more than 0") Long orderId) {

        return ResponseEntity.ok(paymentResponseMapper.toResponseDtoList(paymentService.findAll(orderId)));
    }

    @GetMapping("/{id}")
    ResponseEntity<PaymentResponseDto> findById(@PathVariable
                                                @Positive(message = "must be more than 0") Long id) {
        return ResponseEntity.ok(paymentResponseMapper.toResponseDto(paymentService.findById(id)));
    }

    @PostMapping
    ResponseEntity<PaymentResponseDto> save(@RequestBody @Valid CreatePaymentRequestDto requestDto) {
        CreatePaymentCommand paymentCommand = paymentCommandMapper.toCommand(requestDto);
        Payment payment = paymentService.save(paymentCommand);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(payment.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(paymentResponseMapper.toResponseDto(payment));
    }
}
