--liquibase formatted sql

--changeset mymarket:1
ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id);
ALTER TABLE cart_items DROP CONSTRAINT uq_cart_item_item_id;
ALTER TABLE cart_items ADD CONSTRAINT uq_cart_item_user_item UNIQUE (user_id, item_id);

COMMENT ON COLUMN cart_items.user_id IS 'Ссылка на пользователя-владельца корзины';