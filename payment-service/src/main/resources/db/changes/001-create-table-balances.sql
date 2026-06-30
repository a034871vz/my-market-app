--liquibase formatted sql

--changeset payment:1
CREATE TABLE IF NOT EXISTS payment.balances (
    user_id BIGINT PRIMARY KEY,
    amount BIGINT NOT NULL DEFAULT 10000
);

COMMENT ON TABLE payment.balances IS 'Балансы пользователей';
COMMENT ON COLUMN payment.balances.user_id IS 'ID пользователя из market-service';
COMMENT ON COLUMN payment.balances.amount IS 'Текущий баланс в рублях';
