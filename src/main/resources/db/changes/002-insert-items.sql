--liquibase formatted sql

--changeset mymarket:1
INSERT INTO items (title, description, img_path, price) VALUES
    ('Мяч футбольный', 'Профессиональный футбольный мяч', 'images/ball.png', 1500),
    ('Ракетка теннисная', 'Теннисная ракетка для начинающих', 'images/racket.png', 3200),
    ('Кроссовки беговые', 'Легкие беговые кроссовки', 'images/shoes.png', 5500),
    ('Гантели 2кг', 'Набор гантелей по 2 кг', 'images/dumbbells.png', 1800),
    ('Йога-мат', 'Коврик для йоги и фитнеса', 'images/yogamat.png', 1200),
    ('Велосипед горный', 'Горный велосипед 26 дюймов', 'images/bike.png', 25000),
    ('Скакалка', 'Скакалка со счетчиком', 'images/rope.png', 800),
    ('Фитнес-браслет', 'Умный браслет с пульсометром', 'images/bracelet.png', 4500);