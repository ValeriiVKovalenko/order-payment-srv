package com.valerii.payment.mapper;

import com.valerii.payment.model.command.CreatePaymentCommand;
import com.valerii.payment.model.request.CreatePaymentRequestDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentCommandMapper {

    CreatePaymentCommand toCommand(CreatePaymentRequestDto request);
}
