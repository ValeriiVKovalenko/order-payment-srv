package com.valerii.order.repository;

import com.valerii.order.config.TestcontainersConfiguration;
import com.valerii.order.exception.OrderNotFoundException;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderFilterDto;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.entity.OrderItemEntity;
import com.valerii.order.model.entity.OrderStatusHistoryEntity;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.model.request.PageRequestParamDto;
import com.valerii.order.repository.order.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
@Transactional
@Import(TestcontainersConfiguration.class)
public class OrderRepositoryIntegrationTest {
    @Autowired
    OrderRepository orderRepository;

    @Test
    void find_shouldReturnOptionalEmptyWhenOrderWithDifferentIdExists() {
        OrderItem orderItemTea =
                getOrderItem("Tee", 2, new BigDecimal("50"), new BigDecimal("100"));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, new BigDecimal("50"), new BigDecimal("50"));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        Order order = getOrder(100L, "USD", new BigDecimal("150"), OrderStatus.CREATED, orderItems);
        Long notExistedOrderId = -2L;
        OrderEntity orderEntity = orderRepository.saveOrder(order);
        orderRepository.saveOrderItems(orderItems, orderEntity.getId());

        Optional<OrderEntity> result = orderRepository.findById(notExistedOrderId);

        assertFalse(result.isPresent());
    }

    @Test
    void find_shouldReturnOrderWithItems() {
        OrderItem orderItemTea =
                getOrderItem("Tea", 2, new BigDecimal("50"), new BigDecimal("100"));
        OrderItem orderItemCoffee =
                getOrderItem("Coffee",1, new BigDecimal("50"), new BigDecimal("50"));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItemTea);
        orderItems.add(orderItemCoffee);

        //saved order and orderItems
        Order order = getOrder(100L, "USD", new BigDecimal("150"), OrderStatus.CREATED, orderItems);
        OrderEntity orderEntityWithId = orderRepository.saveOrder(order);
        orderRepository.saveOrderItems(order.getOrderItems(), orderEntityWithId.getId());

        //find order
        OrderEntity orderEntity = orderRepository.findById(orderEntityWithId.getId())
                .orElseThrow();

        //assert Order
        assertNotNull(orderEntity);
        assertEquals(orderEntityWithId.getId(), orderEntity.getId());
        assertEquals(order.getCustomerId(), orderEntity.getCustomerId());
        assertEquals(0, order.getTotalAmount().compareTo(orderEntity.getTotalAmount()));
        assertEquals(order.getStatus().name(), orderEntity.getStatus());
        assertEquals(order.getCurrency(), orderEntity.getCurrency());

        //assert orderItems
        assertNotNull(orderEntity.getOrderItemsEntity());
        assertEquals(order.getOrderItems().size(), orderEntity.getOrderItemsEntity().size());

        //Tea
        OrderItemEntity teeOrderItemEntity = orderEntity.getOrderItemsEntity().stream()
                .filter(productName -> "Tea".equals(productName.getProductName()))
                .findFirst()
                .orElseThrow();

        assertNotNull(teeOrderItemEntity.getId());
        assertEquals(orderItemTea.getProductName(), teeOrderItemEntity.getProductName());
        assertEquals(0, orderItemTea.getUnitPrice().compareTo(teeOrderItemEntity.getUnitPrice()));
        assertEquals(orderItemTea.getQuantity(), teeOrderItemEntity.getQuantity());
        assertEquals(0, orderItemTea.getLineTotal().compareTo(teeOrderItemEntity.getLineTotal()));
        assertEquals(orderEntityWithId.getId(), teeOrderItemEntity.getOrderId());

        //Coffee
        OrderItemEntity coffeeOrderItemEntity = orderEntity.getOrderItemsEntity().stream()
                .filter(productName -> "Coffee".equals(productName.getProductName()))
                .findFirst()
                .orElseThrow();

        assertNotNull(coffeeOrderItemEntity.getId());
        assertEquals(orderItemCoffee.getProductName(), coffeeOrderItemEntity.getProductName());
        assertEquals(0, orderItemCoffee.getUnitPrice().compareTo(coffeeOrderItemEntity.getUnitPrice()));
        assertEquals(orderItemCoffee.getQuantity(), coffeeOrderItemEntity.getQuantity());
        assertEquals(0, orderItemCoffee.getLineTotal().compareTo(coffeeOrderItemEntity.getLineTotal()));
        assertEquals(orderEntityWithId.getId(), coffeeOrderItemEntity.getOrderId());
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findALL_shouldReturnOrderWithFirstPage() {
        Integer page = 0;
        Integer size = 10;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);
        OrderFilterDto orderFilter = getOrderFilter(null, null);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).hasSize(10)
                .extracting(OrderEntity::getId)
                .containsExactly(50L, 49L, 48L, 47L, 46L, 45L, 44L, 43L, 42L, 41L);
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findALL_shouldReturnOrderWithSecondPage() {
        Integer page = 1;
        Integer size = 10;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);
        OrderFilterDto orderFilter = getOrderFilter(null, null);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).hasSize(10)
                .extracting(OrderEntity::getId)
                .containsExactly(40L, 39L, 38L, 37L, 36L, 35L, 34L, 33L, 32L, 31L);
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findALL_shouldReturnOrderOrderByCreatedAt() {
        Integer page = 0;
        Integer size = 50;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);
        OrderFilterDto orderFilter = getOrderFilter(null, null);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).hasSize(50);

        assertThat(orders).extracting(OrderEntity::getCreatedAt)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void count_shouldReturnTotalNumberOfOrders() {
        OrderFilterDto orderFilter = getOrderFilter(null, null);
        Long total = orderRepository.findOrderTotalElements(orderFilter);

        assertThat(total).isEqualTo(50);
    }

    private OrderFilterDto getOrderFilter(Long customerId, OrderStatus status) {
        return OrderFilterDto.builder()
                .customerId(customerId)
                .orderStatus(status)
                .build();
    }

    @Test
    void findALL_shouldReturnEmptyList() {
        Integer page = 0;
        Integer size = 50;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);
        OrderFilterDto orderFilter = getOrderFilter(null, null);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).isEmpty();
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findAll_shouldReturnEmptyListWhenPageIsOutOfRange() {
        int page = 100;
        int size = 10;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);
        OrderFilterDto orderFilter = getOrderFilter(null, null);

        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).isEmpty();
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findAll_shouldReturnLastNonEmptyPage() {
        int page = 4;
        int size = 10;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);

        OrderFilterDto orderFilter = getOrderFilter(null, null);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).hasSize(10);
        assertThat(orders)
                .extracting(OrderEntity::getId)
                .containsExactly(10L, 9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L);
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findAll_shouldReturnOrdersWithoutFilter() {
        int page = 0;
        int size = 20;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);
        List<Long> expected = LongStream.rangeClosed(31, 50)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .toList();

        OrderFilterDto orderFilter = getOrderFilter(null, null);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).hasSize(20);
        assertThat(orders)
                .extracting(OrderEntity::getId)
                .containsExactlyElementsOf(expected);
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findAll_shouldReturnOrdersWithFilterByStatus() {
        int page = 0;
        int size = 20;
        OrderStatus status = OrderStatus.CREATED;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);

        OrderFilterDto orderFilter = getOrderFilter(null, status);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).hasSize(12)
                .isSortedAccordingTo(Comparator.comparing(OrderEntity::getCreatedAt)
                        .reversed()
                        .thenComparing(OrderEntity::getId, Comparator.reverseOrder()));
        assertThat(orders)
                .extracting(OrderEntity::getStatus)
                .containsOnly(status.name());
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findAll_shouldReturnOrdersWithFilterByCustomerId() {
        int page = 0;
        int size = 20;
        Long customerId = 5L;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);

        OrderFilterDto orderFilter = getOrderFilter(customerId, null);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).hasSize(10)
                .isSortedAccordingTo(Comparator.comparing(OrderEntity::getCreatedAt)
                        .reversed()
                        .thenComparing(OrderEntity::getId, Comparator.reverseOrder()));
        assertThat(orders)
                .extracting(OrderEntity::getCustomerId)
                .containsOnly(5L);
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findAll_shouldReturnOrdersWithFilterByStatusAndCustomerId() {
        int page = 0;
        int size = 20;
        OrderStatus status = OrderStatus.CREATED;
        Long customerId = 5L;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);

        OrderFilterDto orderFilter = getOrderFilter(customerId, status);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).hasSize(3)
                .isSortedAccordingTo(Comparator.comparing(OrderEntity::getCreatedAt)
                        .reversed()
                        .thenComparing(OrderEntity::getId, Comparator.reverseOrder()));
        assertThat(orders)
                .extracting(OrderEntity::getCustomerId)
                .containsOnly(5L);
        assertThat(orders)
                .extracting(OrderEntity::getStatus)
                .containsOnly(status.name());
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void count_shouldReturnTotalNumberOfOrdersByFilter() {
        OrderStatus status = OrderStatus.PROCESSING;
        Long customerId = 3L;
        OrderFilterDto orderFilter = getOrderFilter(customerId, status);
        Long total = orderRepository.findOrderTotalElements(orderFilter);

        assertThat(total).isEqualTo(2);
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findAll_shouldReturnEmptyListByFilter() {
        int page = 0;
        int size = 20;
        OrderStatus status = OrderStatus.CREATED;
        Long customerId = 99L;
        PageRequestParamDto pagingParams = new PageRequestParamDto(page, size);

        OrderFilterDto orderFilter = getOrderFilter(customerId, status);
        List<OrderEntity> orders = orderRepository.findAll(orderFilter, size, pagingParams.getOffset());

        assertThat(orders).isEmpty();
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findAll_shouldPaginateAllCreatedOrders_withoutGapsOrDuplicates() {
        //given
        OrderFilterDto filter = new OrderFilterDto(null, OrderStatus.CREATED);
        int size = 5;
        List<Long> expectedIds = List.of(4L, 8L, 12L, 16L, 20L, 24L, 28L, 32L, 36L, 40L, 44L, 48L);

        //when
        Long total = orderRepository.findOrderTotalElements(filter);
        List<OrderEntity> page1 = orderRepository.findAll(filter, size, 0L);
        List<OrderEntity> page2 = orderRepository.findAll(filter, size, 5L);
        List<OrderEntity> page3 = orderRepository.findAll(filter, size, 10L);
        List<OrderEntity> page4 = orderRepository.findAll(filter, size, 15L);

        //then
        assertThat(total).isEqualTo(12L);
        assertThat(page1).hasSize(5);
        assertThat(page2).hasSize(5);
        assertThat(page3).hasSize(2);
        assertThat(page4).isEmpty();

        List<Long> collected = Stream.of(page1, page2, page3)
                .flatMap(List::stream)
                .map(OrderEntity::getId)
                .toList();

        assertThat(collected)
                .doesNotHaveDuplicates()
                .containsExactlyInAnyOrderElementsOf(expectedIds);
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findStatus_shouldReturnStatus() {
        //given
        Long orderId = 1L;
        String expected = "PROCESSING";

        //when
        String actual = orderRepository.findStatus(orderId);

        //then
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void findStatus_shouldThrowException() {
        //given
        Long orderId = -1L;
        String message = "Order with id `" + orderId + "` not found";

        //then
        assertThatThrownBy(() -> orderRepository.findStatus(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage(message);
    }

    @Test
    @Sql("/sql/order/orders-50.sql")
    void updateStatus_shouldUpdateStatusSuccessful() {
        //given
        Long orderId = 1L;
        OrderStatus newUpdateStatus = OrderStatus.COMPLETED;

        //when
        orderRepository.updateStatus(orderId, newUpdateStatus);

        String checkNewStatus = orderRepository.findStatus(1L);

        //then
        assertEquals(newUpdateStatus.name(), checkNewStatus);
    }

    @Test
    @Sql("/sql/status/status-4.sql")
    void findOrderStatusHistory_shouldReturnHistoryCompleted() {
        Long orderId = 2L;

        List<OrderStatusHistoryEntity> orderStatusHistory = orderRepository.findOrderStatusHistory(orderId);

        assertThat(orderStatusHistory)
                .extracting(OrderStatusHistoryEntity::getOldStatus, OrderStatusHistoryEntity::getNewStatus)
                .containsExactly(
                        tuple(OrderStatus.PROCESSING.name(), OrderStatus.COMPLETED.name()),
                        tuple(OrderStatus.CREATED.name(), OrderStatus.PROCESSING.name())
                );
    }

    @Test
    @Sql("/sql/status/status-4.sql")
    void findOrderStatusHistory_shouldReturnHistoryCancelledAfterProcessing() {
        Long orderId = 3L;

        List<OrderStatusHistoryEntity> orderStatusHistory = orderRepository.findOrderStatusHistory(orderId);

        assertThat(orderStatusHistory)
                .extracting(OrderStatusHistoryEntity::getOldStatus, OrderStatusHistoryEntity::getNewStatus)
                .containsExactly(
                        tuple(OrderStatus.PROCESSING.name(), OrderStatus.CANCELLED.name()),
                        tuple(OrderStatus.CREATED.name(), OrderStatus.PROCESSING.name())
                );
    }

    @Test
    @Sql("/sql/status/status-4.sql")
    void findOrderStatusHistory_shouldReturnHistoryCancelledImmediately() {
        Long orderId = 4L;

        List<OrderStatusHistoryEntity> orderStatusHistory = orderRepository.findOrderStatusHistory(orderId);

        assertThat(orderStatusHistory)
                .extracting(OrderStatusHistoryEntity::getOldStatus, OrderStatusHistoryEntity::getNewStatus)
                .containsExactly(
                        tuple(OrderStatus.CREATED.name(), OrderStatus.CANCELLED.name())
                );
    }

    @Test
    @Sql("/sql/status/status-4.sql")
    void findOrderStatusHistory_shouldReturnEmptyList() {
        Long orderId = 1L;

        List<OrderStatusHistoryEntity> orderStatusHistory = orderRepository.findOrderStatusHistory(orderId);

        assertThat(orderStatusHistory).isEmpty();
    }

    @Test
    void createOrderStatusHistory_shouldSaveStatusHistorySuccessfully() {
        //given
        Order order = getOrder(100L, "USD", new BigDecimal("150"), OrderStatus.CREATED,
                List.of(getOrderItem("Tea", 2, new BigDecimal("50"), new BigDecimal("100")),
                        getOrderItem("Coffee",1, new BigDecimal("50"), new BigDecimal("50"))));
        OrderEntity orderEntityWithId = orderRepository.saveOrder(order);
        orderRepository.saveOrderItems(order.getOrderItems(), orderEntityWithId.getId());
        OrderStatus fromStatus = OrderStatus.CREATED;
        OrderStatus toStatus = OrderStatus.PROCESSING;

        //when
        orderRepository.createOrderStatusHistory(orderEntityWithId.getId(), fromStatus, toStatus);

        //then
        List<OrderStatusHistoryEntity> orderStatusHistory =
                orderRepository.findOrderStatusHistory(orderEntityWithId.getId());

        assertThat(orderStatusHistory).extracting(OrderStatusHistoryEntity::getOldStatus, OrderStatusHistoryEntity::getNewStatus)
                .containsExactly(
                        tuple(OrderStatus.CREATED.name(), OrderStatus.PROCESSING.name()));

    }

    private Order getOrder(Long customerId, String currency, BigDecimal totalAmount, OrderStatus status,
                           List<OrderItem> orderItems) {
        return Order.builder()
                .customerId(customerId)
                .currency(currency)
                .totalAmount(totalAmount)
                .status(status)
                .orderItems(orderItems)
                .build();
    }

    private OrderItem getOrderItem(String productName, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        return OrderItem.builder()
                .productName(productName)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .build();
    }
}

