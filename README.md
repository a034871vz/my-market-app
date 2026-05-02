# my-market-app

Веб-приложение «Витрина интернет-магазина» на Spring Boot.

## Технологии

- Java 21
- Spring Boot 3.4.0
- Spring Web MVC
- Spring Data JPA + Hibernate
- Thymeleaf
- PostgreSQL 15 (для разработки)
- H2 (для тестов)
- Liquibase
- Maven
- Docker & Docker Compose
- Lombok

## Функциональность

- Просмотр витрины товаров с пагинацией, поиском и сортировкой
- Карточка товара
- Корзина покупателя (добавление, удаление, изменение количества)
- Оформление заказа
- Просмотр списка заказов и деталей заказа

## Локальный запуск

### Требования

- Java 21
- Maven 3.9+
- Docker & Docker Compose (опционально)

### Вариант 1: Через Maven (с локальной PostgreSQL)

1. Запустите PostgreSQL:
   ```bash
   docker-compose up -d postgres
   ```
2. Соберите и запустите приложение:
   ```bash
   mvn clean package -DskipTests 
   mvn spring-boot:run
   ```

### Вариант 2: Через Docker Compose (полный стек)

   ```bash
      docker-compose up --build   
   ```

### Приложение будет доступно по адресу: http://localhost:8080/items
