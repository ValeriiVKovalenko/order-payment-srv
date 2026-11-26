package com.valerii.payment.mapper;

import com.valerii.payment.model.domain.Payment;
import com.valerii.payment.model.entity.PaymentEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentDomainMapper {

    Payment toDomain(PaymentEntity entity);

    List<Payment> toDomainList(List<PaymentEntity> entities);
}
