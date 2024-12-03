package com.evv.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Класс для хранения параметров с префиксом "app" из application.yml
 */
@Validated
@ConfigurationProperties(prefix = "app")
@Value
public class AppProperties {

    /**
     * Версия приложения для отображения в документации Open API.
     */
    @NotBlank
    String version;

    PaymentSourceProperties payment;

    @Value
    public static class PaymentSourceProperties {

        CreditCardDefProp creditCard;

        AccountDefProp account;

        @Value
        @Validated
        public static class CreditCardDefProp {

            /**
             * Лимит кредитной карты, автоматически созданной для нового пользователя.
             */
            @PositiveOrZero
            int initialLimit;

            /**
             * Срок действия кредитной карты, автоматически созданной для нового пользователя.
             */
            @Min(1)
            int expirationYears;
        }

        @Value
        @Validated
        public static class AccountDefProp {

            /**
             * Начальный баланс банковского счета, автоматически созданного для нового пользователя.
             */
            @PositiveOrZero
            int initialBalance;
        }
    }
}