TRUNCATE TABLE payments RESTART IDENTITY;

INSERT INTO payments (order_id, amount, currency, status, created_at, updated_at)
WITH orders AS (
    SELECT
        o                                                  AS order_id,
        (o * 7) % 31 + 1                                   AS bucket,
    ((500 + (o * 1237) % 49500) / 100.0)::numeric(15, 2) AS amount,
    CASE WHEN o % 4 = 0 THEN 'EUR' ELSE 'USD' END      AS currency,
        timestamptz '2026-01-01 09:00:00+00' + o * interval '47 minutes' AS started_at
    FROM generate_series(1, 31) AS o
),
scenarios AS (
    SELECT
        *,
        CASE
            WHEN bucket <= 11 THEN ARRAY['SUCCEEDED']
            WHEN bucket <= 22 THEN ARRAY['FAILED', 'SUCCEEDED']
            WHEN bucket <= 25 THEN ARRAY['FAILED', 'FAILED', 'SUCCEEDED']
            WHEN bucket <= 28 THEN ARRAY['FAILED']
            WHEN bucket <= 30 THEN ARRAY['PENDING']
            ELSE                   ARRAY['FAILED', 'FAILED', 'FAILED']
        END AS attempts
    FROM orders
)
SELECT
    s.order_id,
    s.amount,
    s.currency,
    a.status,
    s.started_at + (a.attempt_no - 1) * interval '10 minutes'  AS created_at,
    CASE
    WHEN a.status = 'PENDING'
    THEN s.started_at + (a.attempt_no - 1) * interval '10 minutes'
    ELSE s.started_at + (a.attempt_no - 1) * interval '10 minutes' + interval '5 seconds'
END                                                        AS updated_at
FROM scenarios AS s
CROSS JOIN LATERAL unnest(s.attempts) WITH ORDINALITY AS a(status, attempt_no)
ORDER BY created_at, s.order_id;