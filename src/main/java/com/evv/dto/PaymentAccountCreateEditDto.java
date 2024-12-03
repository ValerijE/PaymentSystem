package com.evv.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class PaymentAccountCreateEditDto {

    @NotNull(message = "{validation.NotNull.PaymentAccountCreateEditDto.amount}")
    @PositiveOrZero(message = "{validation.PozitiveOrZero.PaymentAccountCreateEditDto.amount}")
    BigDecimal amount;

    @NotNull(message = "{validation.NotNull.PaymentAccountCreateEditDto.account}")
    AccountDto.Read.Public account;
}
