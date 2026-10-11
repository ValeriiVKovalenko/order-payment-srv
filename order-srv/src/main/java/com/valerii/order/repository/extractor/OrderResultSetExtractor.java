package com.valerii.order.repository.extractor;

import com.valerii.order.model.entity.OrderEntity;
import com.valerii.order.model.entity.OrderItemEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;

public class OrderResultSetExtractor implements ResultSetExtractor<Optional<OrderEntity>> {
    @Override
    public Optional<OrderEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
        OrderEntity order = null;

        while (rs.next()) {
            if (order == null) {
                order = OrderEntity.builder()
                        .id(rs.getLong("order_id"))
                        .customerId(rs.getLong("customer_id"))
                        .status(rs.getString("status"))
                        .totalAmount(rs.getBigDecimal("total_amount"))
                        .currency(rs.getString("currency"))
                        .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                        .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                        .orderItemsEntity(new ArrayList<>())
                        .build();
            }
            Long itemId = rs.getLong("item_id");
            if (itemId != null) {
                OrderItemEntity orderItem = OrderItemEntity.builder()
                        .id(rs.getLong("item_id"))
                        .orderId(rs.getLong("order_item_id"))
                        .productName(rs.getString("product_name"))
                        .quantity(rs.getInt("quantity"))
                        .unitPrice(rs.getBigDecimal("unit_price"))
                        .lineTotal(rs.getBigDecimal("line_total"))
                        .build();
                order.getOrderItemsEntity().add(orderItem);
            }
        }
        return Optional.ofNullable(order);
    }
}
