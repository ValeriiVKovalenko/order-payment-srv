package com.valerii.order.model.request.filter;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
@Builder
public class OrderFilterRequestDto {

    private final Long customerId;
    private final String status;
}
