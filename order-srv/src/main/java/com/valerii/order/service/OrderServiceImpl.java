package com.valerii.order.service;

import com.valerii.order.exception.DomainInvariantViolationException;
import com.valerii.order.exception.InvalidCreateOrderCommandException;
import com.valerii.order.exception.InvalidOrderStatusException;
import com.valerii.order.exception.OrderNotFoundException;
import com.valerii.order.mapper.OrderMapper;
import com.valerii.order.mapper.OrderStatusHistoryMapper;
import com.valerii.order.model.CreateOrderCommand;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderFilterDto;
import com.valerii.order.model.OrderSummaryDto;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.entity.OrderStatusHistoryEntity;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.model.request.PageRequestParamDto;
import com.valerii.order.model.request.UpdateStatusRequestDto;
import com.valerii.order.model.request.filter.OrderFilterRequestDto;
import com.valerii.order.model.response.page.PageResponseDto;
import com.valerii.order.model.response.status.OrderStatusHistoryResponse;
import com.valerii.order.repository.order.OrderRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.valerii.order.model.OrderFactory.createOrder;

@Service
@RequiredArgsConstructor
@Validated
public class OrderServiceImpl implements  OrderService {

    private final Validator validator;
    private final OrderMapper orderMapper;
    private final OrderStatusHistoryMapper orderStatusHistoryMapper;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Order save(CreateOrderCommand createOrderCommand) {
        validateOrderCommand(createOrderCommand);
        Order orderToSave = createOrder(createOrderCommand);
        validateOrderDomain(orderToSave);

        OrderEntity orderEntity = orderRepository.saveOrder(orderToSave);
        orderRepository.saveOrderItems(orderToSave.getOrderItems(), orderEntity.getId());
        Optional<OrderEntity> order = orderRepository.findById(orderEntity.getId());

        return order.map(orderMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("Order not found after save: " + orderEntity.getId()));
    }

    private void validateOrderDomain(Order orderToSave) {
        Set<ConstraintViolation<Order>> orderDomainViolations = validator.validate(orderToSave);
        if (!orderDomainViolations.isEmpty()) {
            throw new DomainInvariantViolationException(orderDomainViolations);
        }
    }

    private void validateOrderCommand(CreateOrderCommand createOrderCommand) {
        Set<ConstraintViolation<CreateOrderCommand>> createOrderCommandViolations = validator.validate(createOrderCommand);
        if (!createOrderCommandViolations.isEmpty()) {
            throw new InvalidCreateOrderCommandException(createOrderCommandViolations);
        }
    }

    @Override
    public Order findById(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return orderMapper.toDomain(orderEntity);
    }

    @Override
    public PageResponseDto<OrderSummaryDto> findAll(OrderFilterRequestDto filter, Integer page, Integer size) {
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);

        OrderStatus status = filter.getStatus() == null
                ? null
                : OrderStatus.fromString(filter.getStatus())
                .orElseThrow(() -> new InvalidOrderStatusException(filter.getStatus()));

        OrderFilterDto orderFilter = new OrderFilterDto(filter.getCustomerId(), status);

        Long totalElements = orderRepository.findOrderTotalElements(orderFilter);
        List<OrderEntity> orders =
                orderRepository.findAll(orderFilter, pagingParams.getSize(), pagingParams.getOffset());
        List<OrderSummaryDto> domainOrderList = orderMapper.toSummaryDtoList(orders);

        return PageResponseDto.of(domainOrderList, size, page, totalElements);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, UpdateStatusRequestDto requestDto) {
        String status = requestDto.getStatus();
        OrderStatus newOrderStatus = OrderStatus.fromString(status)
                .orElseThrow(() -> new InvalidOrderStatusException(status));

        String oldStatus = orderRepository.findStatus(id);
        if (oldStatus.equals(newOrderStatus.name())) {
            return;
        }

        OrderStatus oldOrderStatus = OrderStatus.fromString(oldStatus)
                .orElseThrow(() -> new InvalidOrderStatusException(oldStatus));

        OrderStatus.validateTransitionTo(oldOrderStatus, newOrderStatus);

        orderRepository.updateStatus(id, newOrderStatus);
        orderRepository.createOrderStatusHistory(id, oldOrderStatus, newOrderStatus);
    }

    @Override
    public List<OrderStatusHistoryResponse> findOrderStatusHistory(Long id) {
        boolean isOrderExists = orderRepository.existsById(id);
        if (!isOrderExists) {
            throw new OrderNotFoundException(id);
        }

        List<OrderStatusHistoryEntity> orderStatusHistory = orderRepository.findOrderStatusHistory(id);

        return orderStatusHistoryMapper.toResponseList(orderStatusHistory);
    }
}
