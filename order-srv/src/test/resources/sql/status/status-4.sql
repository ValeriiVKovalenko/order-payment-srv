TRUNCATE TABLE orders RESTART IDENTITY CASCADE;
INSERT INTO orders (id, customer_id, status, total_amount, currency, created_at, updated_at)
OVERRIDING SYSTEM VALUE
VALUES (1, 100, 'CREATED', 10.00, 'USD', '2026-01-01 00:00:00', '2026-01-01 00:00:00');

INSERT INTO orders (id, customer_id, status, total_amount, currency, created_at, updated_at)
OVERRIDING SYSTEM VALUE
VALUES (2, 101, 'CREATED', 100.00, 'USD', '2026-01-01 00:00:00', '2026-01-01 00:00:00');
INSERT INTO order_status_history (order_id, old_status, new_status, changed_at)
VALUES (2, 'CREATED',    'PROCESSING', '2026-01-01 01:00:00'),
       (2, 'PROCESSING', 'COMPLETED',  '2026-01-01 02:00:00');

INSERT INTO orders (id, customer_id, status, total_amount, currency, created_at, updated_at)
OVERRIDING SYSTEM VALUE
VALUES (3, 102, 'CREATED', 50.00, 'USD', '2026-01-01 00:00:00', '2026-01-01 00:00:00');
INSERT INTO order_status_history (order_id, old_status, new_status, changed_at)
VALUES (3, 'CREATED',    'PROCESSING', '2026-01-01 01:00:00'),
       (3, 'PROCESSING', 'CANCELLED',  '2026-01-01 02:00:00');

INSERT INTO orders (id, customer_id, status, total_amount, currency, created_at, updated_at)
OVERRIDING SYSTEM VALUE
VALUES (4, 103, 'CREATED', 250.00, 'USD', '2026-01-01 00:00:00', '2026-01-01 00:00:00');
INSERT INTO order_status_history (order_id, old_status, new_status, changed_at)
VALUES (4, 'CREATED',    'CANCELLED', '2026-01-01 01:00:00');