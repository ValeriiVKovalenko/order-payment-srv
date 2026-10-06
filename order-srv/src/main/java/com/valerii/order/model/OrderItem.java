package com.valerii.order.model;

import java.math.BigDecimal;

public class OrderItem {
    private Long id;
    private Long orderId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
