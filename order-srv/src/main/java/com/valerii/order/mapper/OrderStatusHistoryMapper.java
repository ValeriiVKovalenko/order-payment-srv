package com.valerii.order.mapper;

import com.valerii.order.model.entity.OrderStatusHistoryEntity;
import com.valerii.order.model.response.status.OrderStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderStatusHistoryMapper {

    @Mapping(source = "oldStatus", target = "fromStatus")
    @Mapping(source = "newStatus", target = "toStatus")
    OrderStatusHistoryResponse toResponse(OrderStatusHistoryEntity entity);

    List<OrderStatusHistoryResponse> toResponseList(List<OrderStatusHistoryEntity> entities);
}
