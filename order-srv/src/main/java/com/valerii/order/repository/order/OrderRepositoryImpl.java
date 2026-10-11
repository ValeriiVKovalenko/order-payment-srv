package com.valerii.order.repository.order;

import com.valerii.order.model.Order;
import com.valerii.order.model.OrderItem;
import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.repository.extractor.OrderResultSetExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

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
}
