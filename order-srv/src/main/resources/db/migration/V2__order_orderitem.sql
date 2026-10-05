CREATE TABLE orders
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id   BIGINT NOT NULL,
    status        VARCHAR(20) NOT NULL,
    total_amount  NUMERIC(15,2) NOT NULL,
    currency      CHAR(3) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id     BIGINT NOT NULL,
    product_name VARCHAR(50) NOT NULL,
    quantity     INTEGER NOT NULL ,
    unit_price   NUMERIC(15,2) NOT NULL ,
    line_total   NUMERIC(15,2) NOT NULL
);

ALTER TABLE order_items
    ADD CONSTRAINT FK_orders_order_items
        FOREIGN KEY (order_id) REFERENCES orders(id);

CREATE INDEX order_customer_id_index ON orders (customer_id);
CREATE INDEX order_items_order_id_index ON order_items (order_id);


