--liquibase formatted sql

--changeset mymarket:1
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS 'Пользователи интернет-магазина';
COMMENT ON COLUMN users.id IS 'Идентификатор пользователя';
COMMENT ON COLUMN users.username IS 'Логин пользователя';
COMMENT ON COLUMN users.password IS 'Зашифрованный пароль';
COMMENT ON COLUMN users.role IS 'Роль пользователя';
COMMENT ON COLUMN users.created_at IS 'Дата создания';