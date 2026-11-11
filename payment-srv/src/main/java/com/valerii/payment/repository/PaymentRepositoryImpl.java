package com.valerii.payment.repository;

import com.valerii.payment.model.domen.Payment;
import com.valerii.payment.model.entity.PaymentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<PaymentEntity> PAYMENT_ROW_MAPPER = (rs, rowNum) -> {
        return PaymentEntity.builder()
                .id(rs.getLong("id"))
                .orderId(rs.getLong("order_id"))
                .amount(rs.getBigDecimal("amount"))
                .currency(rs.getString("currency"))
                .status(rs.getString("status"))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                .build();
    };

    @Override
    public PaymentEntity save(Payment payment) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", payment.getOrderId())
                .addValue("amount", payment.getAmount())
                .addValue("currency", payment.getCurrency())
                .addValue("status", payment.getStatus().name());
        String sql = """
                INSERT INTO payments(order_id, amount, currency, status)
                VALUES(:orderId, :amount, :currency, :status)
                RETURNING id, order_id, amount, currency, status, created_at, updated_at
                """;
        return jdbcTemplate.queryForObject(sql, params, PAYMENT_ROW_MAPPER);
    }

    @Override
    public Optional<PaymentEntity> findById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        String sql = """
                SELECT id, order_id, amount, currency, status, created_at, updated_at
                FROM payments
                WHERE id = :id
                """;
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, params, PAYMENT_ROW_MAPPER));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<PaymentEntity> findAllByOrderId(Long orderId) {
        MapSqlParameterSource params = new MapSqlParameterSource("orderId", orderId);
        String sql = """
                SELECT id, order_id, amount, currency, status, created_at, updated_at
                FROM payments
                WHERE order_id = :orderId
                ORDER BY created_at DESC, id DESC
                """;
        return jdbcTemplate.query(sql, params, PAYMENT_ROW_MAPPER);
    }
}
