package com.valerii.order.service;

import com.valerii.order.exception.DomainInvariantViolationException;
import com.valerii.order.exception.InvalidCreateOrderCommandException;
import com.valerii.order.exception.InvalidOrderAmountException;
import com.valerii.order.exception.OrderNotFoundException;
import com.valerii.order.mapper.OrderMapper;
import com.valerii.order.model.CreateOrderCommand;
import com.valerii.order.model.CreateOrderItemCommand;
import com.valerii.order.model.Order;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.repository.order.OrderRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static com.valerii.order.model.OrderFactory.createOrder;

@Service
@RequiredArgsConstructor
@Validated
public class OrderServiceImpl implements  OrderService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999999.99");

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final Validator validator;

    @Override
    @Transactional
    public Order save(CreateOrderCommand createOrderCommand) {
        validateOrderCommand(createOrderCommand);
        validateAmounts(createOrderCommand);

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

    private void validateAmounts(CreateOrderCommand createOrderCommand) {
        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderItemCommand item : createOrderCommand.getOrderItems()) {
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            if (lineTotal.compareTo(MAX_AMOUNT) > 0) {
                throw new InvalidOrderAmountException("Line total", lineTotal, MAX_AMOUNT);
            }
            total = total.add(lineTotal);

            if (total.compareTo(MAX_AMOUNT) > 0) {
                throw new InvalidOrderAmountException("Total amount" ,total, MAX_AMOUNT);
            }
        }
    }

    @Override
    public Order findById(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(String.format("Order with id `%d` not found", orderId)));
        return orderMapper.toDomain(orderEntity);
    }
}
