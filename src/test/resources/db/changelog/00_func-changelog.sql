--liquibase formatted sql

--changeset evv:1 endDelimiter:go
DO
$$
BEGIN
    EXECUTE ( -- Удаление всех таблиц. Происходит в начале поднятия очередного Spring контекста
        SELECT COALESCE (
           (
            SELECT 'DROP TABLE IF EXISTS '
                        || string_agg(format('%I.%I', schemaname, tablename), ', ')
--                         || ' CASCADE'
             FROM pg_catalog.pg_tables
             WHERE schemaname = 'public'
                AND tablename NOT IN ('databasechangelog', 'databasechangeloglock')
            ),
           'SELECT 0'
        )
    );
END
$$;

--changeset evv:2 endDelimiter:go
CREATE OR REPLACE FUNCTION truncate_tables()
    RETURNS void
    LANGUAGE plpgsql AS
$$
BEGIN
    EXECUTE ( -- Удаление данных из всех таблиц и сброс счетчиков. Происходит после каждого теста
        SELECT 'TRUNCATE '
                   || string_agg(format('%I.%I', schemaname, tablename), ', ')
                   || ' RESTART IDENTITY'
        FROM pg_catalog.pg_tables
        WHERE schemaname = 'public'
    );
END
$$;
--rollback DROP FUNCTION truncate_tables