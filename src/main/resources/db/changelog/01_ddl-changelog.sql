--liquibase formatted sql

--changeset evv:1
CREATE TABLE users
(
    id       BIGSERIAL PRIMARY KEY,
    email    VARCHAR(128) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    role VARCHAR(32)  NOT NULL,
    created_at TIMESTAMP,
    modified_at TIMESTAMP,
    created_by VARCHAR(128),
    modified_by VARCHAR(128)
);
--rollback DROP TABLE users;

--changeset evv:2
CREATE TABLE client
(
    users_id BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    birth_date date NOT NULL,
    client_status VARCHAR(32) NOT NULL,
    gender   VARCHAR(8),
    image    VARCHAR(256)
);
--rollback DROP TABLE client;

--changeset evv:3
CREATE TABLE admin
(
    users_id BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    correction_count INT
);
--rollback DROP TABLE admin;

--changeset evv:4
CREATE TABLE credit_card
(
    id              BIGSERIAL PRIMARY KEY,
    expiration_date DATE NOT NULL,
    balance         NUMERIC(20, 4) NOT NULL,
    credit_limit    NUMERIC(20, 4) NOT NULL,
    status          INT NOT NULL,
    client_id       BIGINT NOT NULL REFERENCES client ON DELETE CASCADE,
    created_at TIMESTAMP,
    modified_at TIMESTAMP,
    created_by VARCHAR(128),
    modified_by VARCHAR(128),
    version BIGINT NOT NULL DEFAULT 1
);
--rollback DROP TABLE credit_card;

--changeset evv:5
CREATE TABLE account
(
    id        BIGSERIAL PRIMARY KEY,
    balance   NUMERIC(20, 4) NOT NULL CHECK (balance >= 0 ),
    client_id BIGINT NOT NULL REFERENCES client ON DELETE CASCADE,
    created_at TIMESTAMP,
    modified_at TIMESTAMP,
    created_by VARCHAR(128),
    modified_by VARCHAR(128),
    version BIGINT NOT NULL DEFAULT 1
);
--rollback DROP TABLE account;

--changeset evv:6
CREATE TABLE product
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(124) NOT NULL,
    cost NUMERIC(20, 4) NOT NULL,
    created_at TIMESTAMP,
    modified_at TIMESTAMP,
    created_by VARCHAR(128),
    modified_by VARCHAR(128)
);
--rollback DROP TABLE purchase;

--changeset evv:7
CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(20, 4) NOT NULL
);
--rollback DROP TABLE payment;

--changeset evv:8
CREATE TABLE payment_credit_card (
    payment_id BIGINT PRIMARY KEY REFERENCES payment(id) ON DELETE CASCADE,
    credit_card_id BIGINT REFERENCES credit_card(id) ON DELETE CASCADE
);
--rollback DROP TABLE payment_credit_card;

--changeset evv:9
CREATE TABLE payment_account (
    payment_id BIGINT PRIMARY KEY REFERENCES payment(id) ON DELETE CASCADE,
    account_id BIGINT REFERENCES account(id) ON DELETE CASCADE
);
--rollback DROP TABLE payment_account;

--changeset evv:10
CREATE TABLE purchase (
    id BIGSERIAL PRIMARY KEY ,
    client_id BIGINT NOT NULL REFERENCES client(users_id) ON DELETE CASCADE,
    payment_id BIGINT NOT NULL REFERENCES payment(id) ON DELETE CASCADE,
    created_at TIMESTAMP,
    modified_at TIMESTAMP,
    created_by VARCHAR(128),
    modified_by VARCHAR(128)
);
--rollback DROP TABLE purchase;

--changeset evv:11
CREATE TABLE purchase_products (
    purchase_id BIGINT NOT NULL REFERENCES purchase(id),
    products_id BIGINT NOT NULL REFERENCES product(id)
);
--rollback DROP TABLE purchase_products;

--changeset evv:12
CREATE TABLE revision (
    id BIGSERIAL PRIMARY KEY ,
    timestamp BIGINT NOT NULL
);
--rollback DROP TABLE revision;

--changeset evv:13
CREATE TABLE users_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL REFERENCES revision(id),
    revtype INT2,
    email VARCHAR(128),
    password VARCHAR(128),
    role VARCHAR(32),
    PRIMARY KEY (id, rev)
);
--rollback DROP TABLE users_aud;

--changeset evv:14
CREATE TABLE admin_aud (
    users_id BIGINT NOT NULL,
    rev BIGINT NOT NULL REFERENCES revision(id),
    revtype INT2,
    correction_count INT,
    PRIMARY KEY (users_id, rev)
);
--rollback DROP TABLE admin_aud;

--changeset evv:15
CREATE TABLE client_aud (
    users_id BIGINT,
    rev BIGINT NOT NULL REFERENCES revision(id),
    revtype INT2,
    birth_date date,
    client_status VARCHAR(32),
    gender VARCHAR(8),
    image VARCHAR(256),
    PRIMARY KEY (users_id, rev)
);
--rollback DROP TABLE client_aud;

--changeset evv:16
CREATE TABLE account_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL REFERENCES revision(id),
    revtype INT2,
    balance   NUMERIC(20, 4),
    client_id BIGINT,
    PRIMARY KEY (id, rev)
);
--rollback DROP TABLE account_aud;

--changeset evv:17
CREATE TABLE credit_card_aud (
    id int8 NOT NULL,
    rev BIGINT NOT NULL REFERENCES revision(id),
    revtype INT2,
    expiration_date date,
    balance         NUMERIC(20, 4),
    credit_limit    NUMERIC(20, 4),
    status          INT,
    client_id       BIGINT,
    PRIMARY KEY (id, rev)
);
--rollback DROP TABLE payment_account;

--changeset evv:18
CREATE TABLE purchase_aud (
    id BIGINT NOT NULL,
    client_id BIGINT,
    payment_id BIGINT,
    rev BIGINT NOT NULL REFERENCES revision(id),
    revtype INT2,
    PRIMARY KEY (id, rev)
);
--rollback DROP TABLE payment_account;

--changeset evv:19
CREATE TABLE payment_aud (
    id BIGINT,
    amount NUMERIC(20, 4),
    rev BIGINT NOT NULL REFERENCES revision(id),
    revtype INT2,
    PRIMARY KEY (id, rev)
);
--rollback DROP TABLE payment_account;

--changeset evv:20
CREATE TABLE payment_credit_card_aud (
    payment_id BIGINT,
    credit_card_id BIGINT,
    rev BIGINT NOT NULL REFERENCES revision(id),
    PRIMARY KEY (payment_id, rev)
);
--rollback DROP TABLE payment_account;

--changeset evv:21
CREATE TABLE payment_account_aud (
    payment_id BIGINT,
    account_id BIGINT,
    rev BIGINT NOT NULL REFERENCES revision(id),
    PRIMARY KEY (payment_id, rev)
);
--rollback DROP TABLE payment_account;

--changeset evv:22
CREATE INDEX client_id_idx ON purchase(client_id);
--rollback DROP INDEX "client_id_idx";