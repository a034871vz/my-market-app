--liquibase formatted sql

--changeset mymarket:1
INSERT INTO users (username, password, role) VALUES
 ('user1', '$2a$10$oW6FdLdaeZDXtP/ZO4PbXOeW7vyVRglA8Ev3X0hHpSHInXdl7KNuO', 'USER'),
 ('user2', '$2a$10$oW6FdLdaeZDXtP/ZO4PbXOeW7vyVRglA8Ev3X0hHpSHInXdl7KNuO', 'USER'),
 ('admin', '$2a$10$oW6FdLdaeZDXtP/ZO4PbXOeW7vyVRglA8Ev3X0hHpSHInXdl7KNuO', 'ADMIN');