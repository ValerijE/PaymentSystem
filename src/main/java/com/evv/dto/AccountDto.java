package com.evv.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Экспериментальный вид DTO, в котором все возможные DTO инкапсулированы в одном enum.
 * Особый интерес представляет возможность валидации всех подвидов DTO в одном месте - во внутренних интерфейсах.
 * Материал взят из перевода статьи Стивена Уотермана:
 * <a href="https://habr.com/ru/articles/513072/">Переосмысление DTO в Java</a>
 */
public enum AccountDto {;

    private interface Id { @Positive Long getId(); }
    private interface Balance { @NotNull BigDecimal getBalance(); }
    private interface ClientId { @Positive Long getClientId(); }
    private interface ClntReadDto { @NotNull ClientReadDto getClient(); }


    public enum CreateEdit {;
        @Value
        public static class Public implements Balance, ClientId {
            BigDecimal balance;
            Long clientId;
        }
    }

    public enum Read {;
        @Value public static class Public implements Id, Balance, ClientId {
            Long id;
            BigDecimal balance;
            Long clientId;
        }
    }
}
