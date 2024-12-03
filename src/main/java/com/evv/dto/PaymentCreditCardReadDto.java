package com.evv.dto;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.math.BigDecimal;

@Value
@EqualsAndHashCode(callSuper = true)
public class PaymentCreditCardReadDto extends PaymentReadDto {

    CreditCardDto.Read.Public creditCard;

    public PaymentCreditCardReadDto(Long id, BigDecimal amount, CreditCardDto.Read.Public creditCard) {
        super(id, amount);
        this.creditCard = creditCard;
    }
}
