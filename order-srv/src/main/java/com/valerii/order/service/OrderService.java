package com.valerii.order.service;

import com.valerii.order.model.CreateOrderCommand;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderSummaryDto;
import com.valerii.order.model.request.UpdateStatusRequestDto;
import com.valerii.order.model.request.filter.OrderFilterRequestDto;
import com.valerii.order.model.response.page.PageResponseDto;
import com.valerii.order.model.response.status.OrderStatusHistoryResponse;

import java.util.List;

public interface OrderService {
    Order save(CreateOrderCommand order);

    Order findById(Long orderId);

    PageResponseDto<OrderSummaryDto> findAll(OrderFilterRequestDto filter, Integer page, Integer size);

    void updateStatus(Long id, UpdateStatusRequestDto requestDto);

    List<OrderStatusHistoryResponse> findOrderStatusHistory(Long id);
}
