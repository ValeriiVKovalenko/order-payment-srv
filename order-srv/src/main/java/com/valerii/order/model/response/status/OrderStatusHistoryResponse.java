package com.valerii.order.model.response.status;

import com.valerii.order.model.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;


@Getter
@Builder
@RequiredArgsConstructor
public class OrderStatusHistoryResponse {
    private final OrderStatus fromStatus;
    private final OrderStatus toStatus;
    private final OffsetDateTime changedAt;
}
