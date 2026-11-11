CREATE TABLE order_status_history
(
    order_id     BIGINT NOT NULL,
    old_status   VARCHAR(20) NOT NULL,
    new_status   VARCHAR(20) NOT NULL,
    changed_at   TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

ALTER TABLE order_status_history
    ADD CONSTRAINT FK_orders_order_status_history
        FOREIGN KEY (order_id) REFERENCES orders(id);

CREATE INDEX order_status_history_order_id_index ON order_status_history (order_id);