TRUNCATE TABLE orders RESTART IDENTITY CASCADE;

INSERT INTO orders (customer_id, status, total_amount, currency, created_at, updated_at)
SELECT
    (i % 5) + 1                                               AS customer_id,
    (ARRAY['CREATED', 'PROCESSING', 'COMPLETED', 'CANCELLED'])[(i % 4) + 1] AS status,
    (i * 10.00)::numeric(15, 2)                               AS total_amount,
    'USD'                                                     AS currency,
    timestamp '2026-01-01 00:00:00' + ((i / 5) * interval '1 hour') AS created_at,
    timestamp '2026-01-01 00:00:00' + ((i / 5) * interval '1 hour') AS updated_at
FROM generate_series(1, 50) AS i;