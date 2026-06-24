--liquibase formatted sql

--changeset mymarket:1
ALTER TABLE orders ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id);

COMMENT ON COLUMN orders.user_id IS 'Ссылка на пользователя-оформившего заказ';