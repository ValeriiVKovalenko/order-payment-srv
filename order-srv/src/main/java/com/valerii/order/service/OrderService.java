package com.valerii.order.service;

import com.valerii.order.model.CreateOrderCommand;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderSummaryDto;
import com.valerii.order.model.request.filter.OrderFilterRequestDto;
import com.valerii.order.model.response.page.PageResponseDto;

public interface OrderService {
    Order save(CreateOrderCommand order);

    Order findById(Long orderId);

    PageResponseDto<OrderSummaryDto> findAll(OrderFilterRequestDto filter, Integer page, Integer size);
}
