package com.evv.dto;

import com.evv.database.entity.CreditCardStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Экспериментальный вид DTO, в котором все возможные DTO инкапсулированы в одном enum.
 * Особый интерес представляет возможность валидации всех подвидов DTO в одном месте - во внутренних интерфейсах.
 * Материал взят из перевода статьи Стивена Уотермана:
 * <a href="https://habr.com/ru/articles/513072/">Переосмысление DTO в Java</a>
 */
public enum CreditCardDto {;
    private interface Id { @Positive Long getId(); }
    private interface ExpirationDate { @FutureOrPresent LocalDate getExpirationDate(); }
    private interface Balance { @NotNull BigDecimal getBalance(); }
    private interface CreditLimit { @NegativeOrZero BigDecimal getCreditLimit(); }
    private interface Status { @NotNull CreditCardStatus getStatus(); }
    private interface ClientId { @Positive Long getClientId(); }
//    private interface ClientEmail { @NotBlank String getClientEmail(); }
    private interface ClntReadDto { @NotNull ClientReadDto getClient(); }

    public enum CreateEdit {;
        @Value
        public static class Public implements ExpirationDate, Balance, CreditLimit, Status, ClientId {
            LocalDate expirationDate;
            BigDecimal balance;
            BigDecimal creditLimit;
            CreditCardStatus status;
            Long clientId;
//            String clientEmail;
        }
    }

    public enum Read {;
        @Value public static class Public implements Id, ExpirationDate, Balance, CreditLimit, Status {
            Long id;
            LocalDate expirationDate;
            BigDecimal balance;
            BigDecimal creditLimit;
            CreditCardStatus status;
            Long clientId;
//            ClientReadDto client;
        }
    }
}
