package com.evv;


import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.context.jdbc.Sql;

/**
 * Базовый класс интеграционных тестов.
 * <p>Принято решение отказаться от использования тестовой @Transactional в большинстве интеграционных тестах вопреки
 * рекомендациям пройденных мною обучающих курсов.
 * Данное решение вызвано невозможностью тестирования правильности установки @Transactional в исходниках, т.к.
 * тесты так или иначе успешно завершаются в тестовой @Transactional.
 * <p>В результате удаления тестовой @Transactional скрипт @Sql(..TestData.sql..) стал комититься и для его отката
 * применена хранимая функция truncate_tables
 * <p>При возникновении необходимости выполнения тестового метода в транзакции рекомендуется использовать базовый тестовый
 * класс TransactionalIT. Например, это актуально в случае интеграционных тестов мапперов и репозиториев.
 * <p>Подразумевается, что схема базы данных и хранимые функции накатываются Liquibase в соответствии с db. changelog-master-test.yaml.
 */
@IT
@Sql(scripts = "classpath:sql/TestData.sql")
public abstract class NonTransactionalIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    public void resetDataAndSequences() {
        new SimpleJdbcCall(jdbcTemplate)
                .withFunctionName("truncate_tables")
                .execute();
    }
}
