package com.valerii.order.model.entity;


import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@RequiredArgsConstructor
public class OrderStatusHistoryEntity {
    private final String oldStatus;
    private final String newStatus;
    private final OffsetDateTime changedAt;
}
