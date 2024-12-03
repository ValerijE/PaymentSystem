package com.evv;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Вспомогательный базовый класс интеграционных тестов.
 * <p>Рекомендуется к применению в случае необходимости выполнения тестового метода в транзакции.
 * Примерами могут служить интеграционные тесты мапперов и репозиториев.
 * <p>В остальных случаях рекомендуется применять базовый класс NonTransactionalIT.
 * <p>Подразумевается, что схема базы данных и хранимые функции накатываются Liquibase в соответствии с db.changelog-master-test.yaml.
 * <p>Для сброса счетчиков id БД применена хранимая функция truncate_tables.
 */
@IT
@Sql(scripts = "classpath:sql/TestData.sql")
@Transactional
public abstract class TransactionalIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterTransaction
    protected void resetDataAndSequences() {
        new SimpleJdbcCall(jdbcTemplate)
                .withFunctionName("truncate_tables")
                .execute();
    }
}