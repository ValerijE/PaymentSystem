--liquibase formatted sql

--changeset evv:1
INSERT INTO users (email, password, role, created_at, modified_at, created_by, modified_by)
VALUES ('client1@gmail.com', '{bcrypt}$2y$10$ZEY/A6qbcJUWPyKS3kLyfelOfbKuvPqJ5sudopWbhnRfyH4BVBKJW', 'CLIENT', date '2000-01-20', date '2000-01-20', 'test', 'test'), -- 123 - пароль
       ('client2@gmail.com', '{bcrypt}$2y$10$oAsxrmkR0BD2YgN6peJqGO1GbzC/TburoAxFrnIZ9Bs8gq8uO1s1y', 'CLIENT', date '2001-01-21', date '2001-01-21', 'test', 'test'), -- 321 - пароль
       ('client3@gmail.com', '{bcrypt}$2y$10$zfbBwLO9r.IcdPZ3ZUUkSe7ie/VB/YQKB8IFiv1GYk3L4155Li35i', 'CLIENT', date '1999-12-01', date '1999-12-01', 'test', 'test'), -- 111 - пароль
       ('best_client@gmail.com', '{bcrypt}$2y$10$PlYZBCrJfx7zVFI.TJdaT.w9MHxr7ulKuB9FAZ6.T9RFZyAjHdhji', 'CLIENT', date '1998-12-01', date '1998-12-01', 'test', 'test'), -- 999 - пароль
       ('worst_client@gmail.com', '{bcrypt}$2y$10$O1hI4ZZRQUb6lkSZuZk8WOEEMMQrNjrsKCsEUUq1ra.D1DoCHNUkm', 'CLIENT', date '1997-12-01', date '1997-12-01', 'test', 'test'), -- 111 - пароль
       ('admin1@yandex.ru', '{bcrypt}$2y$10$Fov1L5XEdkSt.YccmV7vtO79NTdL5WKdnMHq5LX8.AfCPIWLwMDBy', 'ADMIN', date '2002-12-01', date '2003-12-01', 'test', 'test'), -- 222 - пароль
       ('admin2@mail.ru', '{bcrypt}$2y$10$jM5uTLIEjTYmjWYjs0hjj.l8ZZQo1PMZCIj12iK9LM08f.0SzX/8K', 'ADMIN', date '2022-12-01', date '2023-12-01', 'test', 'test'); -- 333 - пароль

--changeset evv:2
INSERT INTO client (users_id, birth_date, client_status, gender, image)
VALUES ((SELECT u.id FROM users u WHERE u.email = 'client1@gmail.com'), date '1984-06-15', 'ACTIVE', null, ''),
       ((SELECT u.id FROM users u WHERE u.email = 'client2@gmail.com'), date '1990-01-20', 'ACTIVE', 'MALE', ''),
       ((SELECT u.id FROM users u WHERE u.email = 'client3@gmail.com'), date '1995-12-01', 'BLOCKED', 'FEMALE', ''),
       ((SELECT u.id FROM users u WHERE u.email = 'best_client@gmail.com'), date '1994-12-01', 'ACTIVE', 'MALE', ''),
       ((SELECT u.id FROM users u WHERE u.email = 'worst_client@gmail.com'), date '1993-12-01', 'ACTIVE', 'FEMALE', '');

--changeset evv:3
INSERT INTO admin (users_id, correction_count)
VALUES ((SELECT u.id FROM users u WHERE u.email = 'admin1@yandex.ru'), 0),
       ((SELECT u.id FROM users u WHERE u.email = 'admin2@mail.ru'), 2);

--changeset evv:4
INSERT INTO credit_card (expiration_date, balance, credit_limit, status, client_id)
VALUES ('2024-12-12', '-5000.23'::numeric(12, 4), -150000, 1000, 1),
       ('2024-11-12', -155000, -150000, 2000, 1),
       ('2024-10-12', 100000, 0, 1000, 1),
       ('2026-01-01', -10000.05, -200000, 1000, 2),
       ('2025-03-04', 0, -50000, 1000, 3),
       ('2023-05-06', -10000, -9999, 3000, 1);

--changeset evv:5
INSERT INTO account (balance, client_id)
VALUES (5000.10, 1),
       (10000.50, 1),
       (130000.10, 1),
       (50000.00, 2),
       (0, 3);

--changeset evv:6
INSERT INTO product (name, cost)
VALUES ('Смартфон PX-300 4"', 20000),
       ('Ноутбук ZO-12 14"', 40000),
       ('ПММ FF-60 60 см', 30000),
       ('ВАЗ-2106', 10000),
       ('Удочка бамбук', 5000),
       ('Десктоп ПК-5000', 50000),
       ('Удобрение клубника', 2500),
       ('Сплит-система sum-7', 15000),
       ('Смартфон PX-400 4,5"', 10000),
       ('Ноутбук UI-13 15.6"', 51000),
       ('ПММ FF-45 45 см', 23000),
       ('ВАЗ-2107', 15000),
       ('Удочка углепластик', 12000),
       ('Десктоп ПК-4000', 40000),
       ('Удобрение капуста', 1000),
       ('Сплит-система sum-9', 25000),
       ('Смартфон YD-250 4"', 11000.10),
       ('Ноутбук GG-13 15.6"', 41000),
       ('ПММ FG-100 45 см', 25000),
       ('ВАЗ-2103', 15000),
       ('Удочка стеклоткань', 8500),
       ('Десктоп ПК-3000', 30000),
       ('Удобрение фасоль', 900),
       ('Сплит-система sum-11', 33000),
       ('Смартфон ZI-100 4,5"', 30000),
       ('Ноутбук POI-303 15.6"', 31000),
       ('ПММ ZL-200 60 см', 45000),
       ('ВАЗ-2109', 20000),
       ('Удочка алюминий', 9000),
       ('Десктоп ПК-1000', 10000),
       ('Удобрение горох', 500),
       ('Сплит-система sum-13', 39000),
       ('Смартфон YD-300 4"', 8500),
       ('Ноутбук LI-17 17"', 69500),
       ('ПММ KJ-70 60 см', 40000),
       ('ВАЗ-21099', 30000),
       ('Удочка орешник', 11000),
       ('Десктоп ПК-9000', 91000),
       ('Удобрение щавель', 400),
       ('Сплит-система sum-15', 49000),
       ('Смартфон ZI-350 4,5"', 22222.05),
       ('Ноутбук UI-15 17"', 47000),
       ('ПММ FD-60 60 см', 20000),
       ('ВАЗ-21112', 40000),
       ('Удочка зимняя', 1000),
       ('Десктоп ПК-500i', 150000),
       ('Удобрение томат', 2500),
       ('Сплит-система sum-17', 55500);

--changeset evv:7
INSERT INTO payment (amount)
VALUES (239000.1),
       (41500),
       (10000),
       (23000),
       (2500),
       (2500),
       (15000),
       (20000);

--changeset evv:8
INSERT INTO payment_credit_card (payment_id, credit_card_id)
VALUES (1, 1),
       (2, 3),
       (3, 4),
       (4, 2);

--changeset evv:9
INSERT INTO payment_account (payment_id, account_id)
VALUES (5, 1),
       (6, 1),
       (7, 4),
       (8, 5);

--changeset evv:10
INSERT INTO purchase (client_id, payment_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5),
       (1, 6),
       (2, 7),
       (3, 8);

--changeset evv:11
INSERT INTO purchase_products (purchase_id, products_id)
VALUES (1, 1),
       (1, 2),
       (1, 6),
       (1, 8),
       (1, 10),
       (1, 13),
       (1, 17),
       (1, 19),
       (1, 20),
       (2, 21),
       (2, 24),
       (3, 9),
       (4, 11),
       (5, 7),
       (6, 7),
       (7, 25),
       (7, 30),
       (8, 35),
       (8, 40);

