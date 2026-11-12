package com.valerii.payment.mapper;

import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.response.PaymentResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentResponseMapper {

    PaymentResponseDto toResponseDto(Payment payment);
}
