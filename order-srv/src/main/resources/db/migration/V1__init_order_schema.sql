CREATE TABLE health_check
(
    id          SERIAL PRIMARY KEY,
    service_name CHARACTER VARYING(30) NOT NULL,
    created_at    TIMESTAMP DEFAULT NOW() NOT NULL
);

INSERT INTO health_check (service_name)
VALUES ('order-srv');