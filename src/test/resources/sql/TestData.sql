INSERT INTO users (email, password, role, created_at, modified_at, created_by, modified_by)
VALUES ('test-client-email1@gmail.com', '{noop}156', 'CLIENT', date '2004-01-21', date '2005-02-23', 'test', 'test'),
       ('test-client-email2@yandex.ru', '{noop}0016', 'CLIENT', date '2001-11-21', date '2001-12-21', 'test', 'test'),
       ('test-admin-email1@yandex.ru', '{noop}222', 'ADMIN', date '2002-12-01', date '2003-12-01', 'test', 'test'),
       ('test-admin-email2@mail.ru', '{noop}333', 'ADMIN', date '2022-12-01', date '2023-12-01', 'test', 'test');

INSERT INTO client (users_id, birth_date, client_status, gender, image)
VALUES ((SELECT u.id FROM users u WHERE u.email = 'test-client-email1@gmail.com'), date '1974-06-15', 'ACTIVE', null, ''),
       ((SELECT u.id FROM users u WHERE u.email = 'test-client-email2@yandex.ru'), date '1980-01-20', 'BLOCKED', 'MALE', '');

INSERT INTO admin (users_id, correction_count)
VALUES ((SELECT u.id FROM users u WHERE u.email = 'test-admin-email1@yandex.ru'), 0),
       ((SELECT u.id FROM users u WHERE u.email = 'test-admin-email2@mail.ru'), 2);

INSERT INTO credit_card (expiration_date, balance, credit_limit, status, client_id)
VALUES ('2026-01-01', -1000.50, -30000, 1000,  (SELECT u.id FROM users u WHERE u.email = 'test-client-email1@gmail.com')),
       ('2025-01-15',  -50000.00, -50000, 2000,  (SELECT u.id FROM users u WHERE u.email = 'test-client-email1@gmail.com')),
       ('2024-03-09',  0.00, -50000, 1000,  (SELECT u.id FROM users u WHERE u.email = 'test-client-email2@yandex.ru')),
       ('2023-07-17',  -70000.00, -50000, 3000,  (SELECT u.id FROM users u WHERE u.email = 'test-client-email2@yandex.ru'));

INSERT INTO account (balance, client_id)
VALUES (25000.10, (SELECT u.id FROM users u WHERE u.email = 'test-client-email1@gmail.com')),
       (50000.00, (SELECT u.id FROM users u WHERE u.email = 'test-client-email2@yandex.ru')),
       (0, (SELECT u.id FROM users u WHERE u.email = 'test-client-email1@gmail.com')),
       (200000.00, (SELECT u.id FROM users u WHERE u.email = 'test-client-email1@gmail.com'));

INSERT INTO payment (amount)
VALUES (5000), (10000), (15000), (20000), (25000);

INSERT INTO payment_credit_card (payment_id, credit_card_id)
VALUES ((SELECT id FROM payment p WHERE p.amount = 5000), (SELECT id FROM credit_card cc WHERE cc.client_id = (SELECT u.id FROM users u WHERE u.email = 'test-client-email1@gmail.com') LIMIT 1 OFFSET 0)),
       ((SELECT id FROM payment p WHERE p.amount = 10000), (SELECT id FROM credit_card cc WHERE cc.client_id = (SELECT u.id FROM users u WHERE u.email = 'test-client-email2@yandex.ru') LIMIT 1 OFFSET 1)),
       ((SELECT id FROM payment p WHERE p.amount = 25000), (SELECT id FROM credit_card cc WHERE cc.client_id = (SELECT u.id FROM users u WHERE u.email = 'test-client-email1@gmail.com') LIMIT 1 OFFSET 0));

INSERT INTO payment_account (payment_id, account_id)
VALUES ((SELECT id FROM payment p WHERE p.amount = 15000), (SELECT id FROM account a WHERE a.client_id = (SELECT u.id FROM users u WHERE u.email = 'test-client-email1@gmail.com') LIMIT 1 OFFSET 0)),
       ((SELECT id FROM payment p WHERE p.amount = 20000), (SELECT id FROM account a WHERE a.client_id = (SELECT u.id FROM users u WHERE u.email = 'test-client-email2@yandex.ru') LIMIT 1 OFFSET 1));

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
       ('ПММ FG-100 45 см', 25000);

INSERT INTO purchase (client_id, payment_id, created_at, modified_at, created_by, modified_by)
VALUES  ((SELECT u.id FROM users u WHERE u.email = 'test-client-email1@gmail.com'),
         (SELECT id FROM payment p WHERE p.amount = 5000), null, null, null ,null),
        ((SELECT u.id FROM users u WHERE u.email = 'test-client-email1@gmail.com'),
         (SELECT id FROM payment p WHERE p.amount = 15000), null, null, null ,null),
        ((SELECT u.id FROM users u WHERE u.email = 'test-client-email2@yandex.ru'),
         (SELECT id FROM payment p WHERE p.amount = 10000), null, null, null ,null),
        ((SELECT u.id FROM users u WHERE u.email = 'test-client-email2@yandex.ru'),
         (SELECT id FROM payment p WHERE p.amount = 20000), null, null, null ,null),
        ((SELECT u.id FROM users u WHERE u.email = 'test-client-email1@gmail.com'),
         (SELECT id FROM payment p WHERE p.amount = 25000), null, null, null ,null);

INSERT INTO purchase_products (purchase_id, products_id)
VALUES ((SELECT p.id FROM purchase p WHERE p.payment_id = (SELECT id FROM payment p WHERE p.amount = 5000)),
        (SELECT p.id FROM product p WHERE p.name LIKE 'Удочка бамбук')),
       ((SELECT p.id FROM purchase p WHERE p.payment_id = (SELECT id FROM payment p WHERE p.amount = 5000)),
        (SELECT p.id FROM product p WHERE p.name LIKE 'Удобрение клубника')),
       ((SELECT p.id FROM purchase p WHERE p.payment_id = (SELECT id FROM payment p WHERE p.amount = 15000)),
        (SELECT p.id FROM product p WHERE p.name LIKE 'ВАЗ-2106')),
       ((SELECT p.id FROM purchase p WHERE p.payment_id = (SELECT id FROM payment p WHERE p.amount = 15000)),
        (SELECT p.id FROM product p WHERE p.name LIKE 'Удочка бамбук')),
       ((SELECT p.id FROM purchase p WHERE p.payment_id = (SELECT id FROM payment p WHERE p.amount = 10000)),
        (SELECT p.id FROM product p WHERE p.name LIKE 'Смартфон PX-400 4,5"')),
       ((SELECT p.id FROM purchase p WHERE p.payment_id = (SELECT id FROM payment p WHERE p.amount = 20000)),
        (SELECT p.id FROM product p WHERE p.name LIKE 'Смартфон PX-300 4"')),
       ((SELECT p.id FROM purchase p WHERE p.payment_id = (SELECT id FROM payment p WHERE p.amount = 25000)),
        (SELECT p.id FROM product p WHERE p.name LIKE 'Сплит-система sum-9'));
