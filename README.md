# my-market-app

Мультимодульное веб-приложение «Витрина интернет-магазина» на Spring Boot с сервисом платежей и кешированием в Redis.

## Структура проекта

Проект состоит из двух подпроектов:

| Подпроект         | Описание                                            |
| ----------------- | --------------------------------------------------- |
| `market-service`  | Основное веб-приложение «Витрина интернет-магазина» |
| `payment-service` | RESTful-сервис платежей                             |

## Технологии

* Java 21
* Spring Boot 3.4.0
* Spring WebFlux
* Spring Data R2DBC
* PostgreSQL
* Spring Data Redis
* Thymeleaf
* OpenAPI Generator
* Liquibase
* Testcontainers
* Gradle Kotlin DSL
* Docker & Docker Compose
* Lombok

## Функциональность

### market-service

* Просмотр витрины товаров с пагинацией
* Поиск и сортировка товаров
* Просмотр карточки товара
* Управление корзиной
* Оформление заказа
* Просмотр истории заказов
* Кеширование товаров в Redis

### payment-service

* Получение баланса счёта
* Списание денежных средств

## Интеграция сервисов

Интеграция между сервисами реализована через реактивный HTTP-клиент, сгенерированный по OpenAPI-спецификации.

## Локальный запуск

### Требования

* Java 21
* Gradle 8.5+
* Docker
* Docker Compose

### Запуск инфраструктуры

```bash
docker-compose up -d postgres redis
```

### Сборка проекта

```bash
./gradlew build
```

### Запуск payment-service

```bash
./gradlew :payment-service:bootRun
```

### Запуск market-service

```bash
./gradlew :market-service:bootRun
```

## Запуск через Docker Compose

```bash
docker-compose up --build
```

## Доступ к сервисам

| Сервис                    | URL                         |
| ------------------------- | --------------------------- |
| Витрина интернет-магазина | http://localhost:8080/items |
| Сервис платежей           | http://localhost:8081       |

## Тестирование

### Все тесты

```bash
./gradlew test
```

### Тесты market-service

```bash
./gradlew :market-service:test
```

### Тесты payment-service

```bash
./gradlew :payment-service:test
```

## Кеширование

Используются следующие ключи Redis:

| Ключ              | Назначение         | TTL      |
| ----------------- | ------------------ | -------- |
| `item:{id}`       | Карточка товара    | 2 минуты |
| `items:content:*` | Список товаров     | 2 минуты |
| `items:count:*`   | Количество товаров | 2 минуты |

При отсутствии данных в кеше они загружаются из PostgreSQL и сохраняются в Redis.

## OpenAPI

Спецификация находится по пути:

```text
market-service/src/main/resources/openapi/payment-api.yaml
```

При сборке проекта автоматически генерируются:

* клиент для `market-service`;
* серверный код для `payment-service`.
