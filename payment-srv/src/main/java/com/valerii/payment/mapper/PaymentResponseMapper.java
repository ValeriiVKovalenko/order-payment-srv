package com.valerii.payment.mapper;

import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.response.PaymentResponseDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentResponseMapper {

    PaymentResponseDto toResponseDto(Payment payment);

    List<PaymentResponseDto> toResponseDtoList(List<Payment> payments);
}
