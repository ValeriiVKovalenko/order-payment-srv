package com.valerii.order.repository.order;

import com.valerii.order.exception.OrderNotFoundException;
import com.valerii.order.model.Order;
import com.valerii.order.model.OrderFilterDto;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.entity.OrderStatusHistoryEntity;
import com.valerii.order.model.enums.OrderStatus;
import com.valerii.order.repository.extractor.OrderResultSetExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private static final String ORDERS_FROM = "FROM orders";
    public static final RowMapper<OrderEntity> ORDER_ROW_MAPPER = (rs, rowNum) -> {
        return OrderEntity.builder()
                .id(rs.getLong("id"))
                .customerId(rs.getLong("customer_id"))
                .status(rs.getString("status"))
                .totalAmount(rs.getBigDecimal("total_amount"))
                .currency(rs.getString("currency"))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                .build();
    };

    public static final RowMapper<OrderStatusHistoryEntity> ORDER_STATUS_HISTORY_ROW_MAPPER = (rs, rowNum) -> {
        return OrderStatusHistoryEntity.builder()
                .oldStatus(rs.getString("old_status"))
                .newStatus(rs.getString("new_status"))
                .changedAt(rs.getObject("changed_at", OffsetDateTime.class))
                .build();
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<OrderEntity> findById(Long id) {
        String sql = """
                SELECT O.id as order_id,
                       O.customer_id,
                       O.status,
                       O.total_amount,
                       O.currency,
                       O.created_at,
                       O.updated_at,
                       OI.id as item_id,
                       OI.order_id as order_item_id,
                       OI.product_name,
                       OI.quantity,
                       OI.unit_price,
                       OI.line_total
                FROM orders O
                LEFT JOIN order_items OI ON OI.order_id = O.id
                WHERE O.id = :order_id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("order_id", id);
        return jdbcTemplate.query(sql, params, new OrderResultSetExtractor());
    }

    @Override
    public List<OrderEntity> findAll(OrderFilterDto filter, Integer size, Long offset) {
        MapSqlParameterSource params = new MapSqlParameterSource("size", size)
                .addValue("offset", offset);

        String sql = "SELECT id, "
                + "customer_id, "
                + "status,"
                + "total_amount,"
                + "currency,"
                + "created_at,"
                + "updated_at "
                + ORDERS_FROM + buildWhereClause(filter, params)
                + " ORDER BY created_at DESC, id DESC "
                + "LIMIT :size OFFSET :offset";

        return jdbcTemplate.query(sql, params, ORDER_ROW_MAPPER);
    }

    @Override
    public Long findOrderTotalElements(OrderFilterDto filter) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = "SELECT count(*) " + ORDERS_FROM + buildWhereClause(filter, params);

        return jdbcTemplate.queryForObject(sql, params, Long.class);
    }

    @Override
    public String findStatus(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        String sql = """
                SELECT status
                FROM orders
                WHERE id = :id FOR UPDATE
                """;
        try {
            return jdbcTemplate.queryForObject(sql, params, String.class);
        } catch (EmptyResultDataAccessException ex) {
            throw new OrderNotFoundException(id);
        }
    }

    @Override
    public void updateStatus(Long id, OrderStatus newOrderStatus) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", newOrderStatus.name());

        String sql = """
                UPDATE orders
                SET status = :status,
                updated_at = clock_timestamp()
                WHERE id = :id
                """;
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void createOrderStatusHistory(Long id, OrderStatus oldOrderStatus, OrderStatus newOrderStatus) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("oldStatus", oldOrderStatus.name())
                .addValue("newStatus", newOrderStatus.name())
                .addValue("orderId", id);
        String sql = """
                INSERT INTO order_status_history (order_id, old_status, new_status, changed_at)
                VALUES(:orderId, :oldStatus, :newStatus, clock_timestamp())
                """;
        jdbcTemplate.update(sql, params);
    }

    @Override
    public List<OrderStatusHistoryEntity> findOrderStatusHistory(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        String sql = """
                SELECT
                old_status,
                new_status,
                changed_at
                FROM order_status_history
                WHERE order_id = :id
                ORDER BY changed_at DESC, id DESC
                """;
        return jdbcTemplate.query(sql, params, ORDER_STATUS_HISTORY_ROW_MAPPER);
    }

    @Override
    public boolean existsById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        String sql = """
                SELECT EXISTS (
                                SELECT 1
                                FROM orders
                                WHERE id = :id
                            )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public OrderEntity saveOrder(Order order) {
        String sql = """
                INSERT INTO orders (customer_id, status, total_amount, currency)
                VALUES (:customerId, :status, :totalAmount, :currency)
                RETURNING id, customer_id, status, total_amount, currency, created_at, updated_at
                """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("customerId", order.getCustomerId());
        params.addValue("status", order.getStatus().name());
        params.addValue("totalAmount", order.getTotalAmount());
        params.addValue("currency", order.getCurrency());
        return jdbcTemplate.queryForObject(sql, params, ORDER_ROW_MAPPER);
    }

    @Override
    public void saveOrderItems(List<OrderItem> orderItems, Long orderId) {
        String sql = """
                INSERT INTO order_items(order_id, product_name, quantity, unit_price, line_total)
                VALUES (:orderId, :productName, :quantity, :unitPrice, :lineTotal)
                """;
       SqlParameterSource[] batchParams = orderItems.stream()
               .map(item -> new MapSqlParameterSource()
                       .addValue("orderId", orderId)
                       .addValue("productName", item.getProductName())
                       .addValue("quantity", item.getQuantity())
                       .addValue("unitPrice", item.getUnitPrice())
                       .addValue("lineTotal", item.getLineTotal()))
               .toArray(SqlParameterSource[]::new);
       jdbcTemplate.batchUpdate(sql, batchParams);
    }

    private String buildWhereClause(OrderFilterDto filter, MapSqlParameterSource params) {
        List<String> conditions = new ArrayList<>();
        if (filter.getCustomerId() != null) {
            conditions.add("customer_id = :customerId");
            params.addValue("customerId", filter.getCustomerId());
        }

        if (filter.getOrderStatus() != null) {
            conditions.add("status = :status");
            params.addValue("status", filter.getOrderStatus().name());
        }

        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    }
}
