--liquibase formatted sql

--changeset mymarket:1
CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    img_path VARCHAR(500),
    price BIGINT NOT NULL
);

COMMENT ON TABLE items IS 'Товары интернет-магазина';
COMMENT ON COLUMN items.id IS 'Идентификатор товара';
COMMENT ON COLUMN items.title IS 'Название товара';
COMMENT ON COLUMN items.description IS 'Описание товара';
COMMENT ON COLUMN items.img_path IS 'Путь к изображению товара';
COMMENT ON COLUMN items.price IS 'Цена товара в рублях';

--changeset mymarket:2
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    count INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_cart_item_item_id UNIQUE (item_id)
);

COMMENT ON TABLE cart_items IS 'Товары в корзине покупателя';
COMMENT ON COLUMN cart_items.id IS 'Идентификатор записи корзины';
COMMENT ON COLUMN cart_items.item_id IS 'Ссылка на товар';
COMMENT ON COLUMN cart_items.count IS 'Количество товара в корзине';

--changeset mymarket:3
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    total_sum BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE orders IS 'Заказы покупателей';
COMMENT ON COLUMN orders.id IS 'Идентификатор заказа';
COMMENT ON COLUMN orders.total_sum IS 'Суммарная стоимость заказа в рублях';

--changeset mymarket:4
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    price BIGINT NOT NULL,
    count INT NOT NULL
);

COMMENT ON TABLE order_items IS 'Товары в составе заказа';
COMMENT ON COLUMN order_items.id IS 'Идентификатор записи';
COMMENT ON COLUMN order_items.order_id IS 'Ссылка на заказ';
COMMENT ON COLUMN order_items.item_id IS 'Идентификатор товара на момент заказа';
COMMENT ON COLUMN order_items.title IS 'Название товара на момент заказа';
COMMENT ON COLUMN order_items.price IS 'Цена товара на момент заказа';
COMMENT ON COLUMN order_items.count IS 'Количество товара в заказе';

--changeset mymarket:5
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
