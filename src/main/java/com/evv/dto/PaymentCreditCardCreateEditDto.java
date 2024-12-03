package com.evv.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class PaymentCreditCardCreateEditDto {

    @NotNull(message = "{validation.NotNull.PaymentCreditCardCreateEditDto.amount}")
    @PositiveOrZero(message = "{validation.PozitiveOrZero.PaymentCreditCardCreateEditDto.amount}")
    BigDecimal amount;

    @NotNull(message = "{validation.NotNull.PaymentCreditCardCreateEditDto.creditCard}")
    CreditCardDto.Read.Public creditCard;
}
