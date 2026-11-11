package com.valerii.order.mapper;

import com.valerii.order.model.entity.OrderStatusHistoryEntity;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.model.response.status.OrderStatusHistoryResponse;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class OrderStatusHistoryMapperTest {
    private static final OffsetDateTime BASE =
            OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private final OrderStatusHistoryMapper orderStatusHistoryMapper = new OrderStatusHistoryMapperImpl();

    @Test
    void should_correctlyMapFromEntityToResponseList() {
        //given
        List<OrderStatusHistoryEntity> orderStatusHistoryEntities = getHistoryEntityCompleted();

        //when
        List<OrderStatusHistoryResponse> result = orderStatusHistoryMapper.toResponseList(orderStatusHistoryEntities);

        //then
        assertThat(result).extracting(OrderStatusHistoryResponse::getFromStatus, OrderStatusHistoryResponse::getToStatus, OrderStatusHistoryResponse::getChangedAt)
                .containsExactly(
                        tuple(OrderStatus.PROCESSING, OrderStatus.COMPLETED, OffsetDateTime.of(2026, 1, 1, 1, 0, 0, 0, ZoneOffset.UTC)),
                        tuple(OrderStatus.CREATED, OrderStatus.PROCESSING, OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)));
    }

    private List<OrderStatusHistoryEntity> getHistoryEntityCompleted() {
        return List.of(
                getHistoryEntity(OrderStatus.PROCESSING.name(), OrderStatus.COMPLETED.name(), BASE.plusHours(1)),
                getHistoryEntity(OrderStatus.CREATED.name(), OrderStatus.PROCESSING.name(), BASE)
        );
    }

    private OrderStatusHistoryEntity getHistoryEntity(
            String from, String to, OffsetDateTime changedAt) {
        return OrderStatusHistoryEntity.builder()
                .oldStatus(from)
                .newStatus(to)
                .changedAt(changedAt)
                .build();
    }
}
